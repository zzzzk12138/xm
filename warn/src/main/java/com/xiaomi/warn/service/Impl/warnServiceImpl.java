package com.xiaomi.warn.service.Impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.Vehicle;
import com.xiaomi.warn.entity.Warn;
import com.xiaomi.warn.entity.WarnRule;
import com.xiaomi.warn.entity.BatteryType;
import com.xiaomi.warn.mapper.vehicleMapper;
import com.xiaomi.warn.mapper.warnMapper;
import com.xiaomi.warn.processor.SignalProcessor;
import com.xiaomi.warn.processor.impl.VoltageProcessor;
import com.xiaomi.warn.processor.impl.CurrentProcessor;
import com.xiaomi.warn.service.warnService;
import com.xiaomi.warn.service.vehicleService;
import com.xiaomi.warn.service.warnRuleService;
import com.xiaomi.warn.task.warnSignalProviderTask;
import com.xiaomi.warn.utils.WarnResultBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class warnServiceImpl implements warnService {

    private static final int BATCH_SIZE = 100; // 设置批处理大小
    private static final int CORE_POOL_SIZE = 4; // 核心线程数
    private static final int MAX_POOL_SIZE = 8; // 最大线程数
    private static final int QUEUE_CAPACITY = 100; // 队列容量
    private static final long KEEP_ALIVE_TIME = 60L; // 线程空闲时间

    private ExecutorService executorService;

    @PostConstruct
    public void init() {
        // 创建线程池
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAX_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadPoolExecutor.CallerRunsPolicy() // 队列满时，由调用线程执行任务
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        this.executorService = executor;
    }

    @PreDestroy
    public void destroy() {
        // 优雅关闭线程池
        if (executorService != null) {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    @Autowired
    private warnMapper warnMapper;

    @Autowired
    private vehicleService vehicleService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private vehicleMapper vehicleMapper;

    @Autowired
    private warnSignalProviderTask warnSignalProviderTask;

    @Autowired
    private VoltageProcessor voltageProcessor;

    @Autowired
    private CurrentProcessor currentProcessor;

    @Autowired
    private warnRuleService warnRuleService;

    @Override
    public List<Map<String, Object>> processWarns(List<warnDTO> warnDTOList) {
        List<Map<String, Object>> allResults = Collections.synchronizedList(new ArrayList<>());
        Set<Integer> allCarIds = Collections.synchronizedSet(new HashSet<>());
        
        long start = System.currentTimeMillis();
        
        try {
            // 分批并提交到线程池
            List<CompletableFuture<ProcessBatchResult>> futures = Lists.partition(warnDTOList, BATCH_SIZE)
                .stream()
                .map(batch -> CompletableFuture.supplyAsync(() -> processBatch(batch), executorService))
                .collect(Collectors.toList());

            // 等待所有批次处理完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // 收集结果
            for (CompletableFuture<ProcessBatchResult> future : futures) {
                try {
                    ProcessBatchResult result = future.get();
                    allResults.addAll(result.getResults());
                    allCarIds.addAll(result.getCarIds());
                } catch (Exception e) {
                    log.error("获取批处理结果失败: {}", e.getMessage(), e);
                }
            }

            // 发送到MQ
            if (!allCarIds.isEmpty()) {
                try {
                    warnSignalProviderTask.sendVidsToMQ(allCarIds);
                } catch (Exception e) {
                    log.error("发送警告信息到MQ失败: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("并行处理警告信息失败: {}", e.getMessage(), e);
        }

        long end = System.currentTimeMillis();
        log.info("处理{}条数据耗时: {}ms", warnDTOList.size(), end - start);
        
        return allResults;
    }

    /**
     * 处理一批数据的结果
     */
    private static class ProcessBatchResult {
        private final List<Map<String, Object>> results;
        private final Set<Integer> carIds;

        public ProcessBatchResult(List<Map<String, Object>> results, Set<Integer> carIds) {
            this.results = results;
            this.carIds = carIds;
        }

        public List<Map<String, Object>> getResults() {
            return results;
        }

        public Set<Integer> getCarIds() {
            return carIds;
        }
    }

    /**
     * 处理一批数据
     * 注意：此方法会在多个线程中并行执行
     */
    private ProcessBatchResult processBatch(List<warnDTO> batch) {
        List<Map<String, Object>> results = new ArrayList<>();
        Set<Integer> carIds = new HashSet<>();
        List<SignalProcessor> processors = Arrays.asList(voltageProcessor, currentProcessor);
        
        for (warnDTO warn : batch) {
            try {
                carIds.add(warn.getCarId());
                BatteryType batteryType = vehicleService.getBatteryTypeByCarId(warn.getCarId());
                Map<String, Double> signalMap = objectMapper.readValue(warn.getSignal(), Map.class);

                for (SignalProcessor processor : processors) {
                    Map<String, Object> result = processor.process(warn, batteryType, signalMap);
                    if (result != null) {
                        results.add(result);
                    }
                }
            } catch (Exception e) {
                log.error("处理警告信息失败: carId={}, error={}", warn.getCarId(), e.getMessage(), e);
                results.add(new WarnResultBuilder()
                        .setCarId(warn.getCarId())
                        .setError("处理失败: " + e.getMessage())
                        .build());
            }
        }
        
        return new ProcessBatchResult(results, carIds);
    }

    @Override
    public List<Map<String, Object>> getWarnsByCarId(Integer carId) {
        log.info("开始查询车辆预警信息: carId={}", carId);
        List<Map<String, Object>> results = new ArrayList<>();
        
        try {
            Vehicle vehicleByCarId = vehicleService.getVehicleByCarId(carId);
            if (vehicleByCarId == null) {
                log.error("查询车辆预警信息失败：未找到车辆信息, carId={}", carId);
                Map<String, Object> errorResult = new HashMap<>();
                errorResult.put("车架编号", carId);
                errorResult.put("error", "未找到车辆信息");
                results.add(errorResult);
                return results;
            }

            List<Warn> warns = warnMapper.findWarnsByCarId(carId);
            
            for (Warn warn : warns) {
                Map<String, Object> result = new HashMap<>();
                result.put("车架编号", warn.getCarId());
                result.put("电池类型", warn.getBatTypename());
                result.put("预警名称", warn.getWarnName());
                result.put("预警等级", warn.getWarnLevel());
                result.put("创建时间", warn.getCreatedAt());
                results.add(result);
            }
            
            log.info("成功查询车辆预警信息: carId={}, 预警数量={}", carId, warns.size());
        } catch (Exception e) {
            log.error("查询车辆预警信息失败: carId={}, error={}", carId, e.getMessage(), e);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("车架编号", carId);
            errorResult.put("error", "查询失败: " + e.getMessage());
            results.add(errorResult);
        }
        
        return results;
    }
}
