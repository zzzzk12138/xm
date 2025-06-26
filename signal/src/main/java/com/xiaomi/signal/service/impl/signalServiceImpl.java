package com.xiaomi.signal.service.impl;

import com.xiaomi.signal.entity.Signal;
import com.xiaomi.signal.mapper.signalMapper;
import com.xiaomi.signal.service.signalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.integration.redis.util.RedisLockRegistry;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.ArrayList;

@Slf4j
@Service
public class signalServiceImpl implements signalService {
    
    @Autowired
    private signalMapper signalMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisLockRegistry redisLockRegistry;

    // Redis key 前缀
    private static final String REDIS_KEY_PREFIX = "signal:";
    // 缓存基础过期时间（秒）
    private static final long CACHE_EXPIRE_TIME = 60;
    // 缓存过期时间随机波动范围（秒）
    private static final long CACHE_EXPIRE_RANDOM_RANGE = 3;
    // 分布式锁超时时间（秒）
    private static final long LOCK_TIMEOUT = 10;

    /**
     * 获取带随机波动的缓存过期时间，防止缓存雪崩
     * 60 ± 3秒
     * @return 实际的缓存过期时间（秒）
     */
    private long getRandomExpireTime() {
        return CACHE_EXPIRE_TIME + (long) (Math.random() * 2 * CACHE_EXPIRE_RANDOM_RANGE - CACHE_EXPIRE_RANDOM_RANGE);
    }

    /**
     * 生成Redis缓存key
     * @param prefix 前缀标识，如 "id", "vid" 等
     * @param value 具体的值
     * @return Redis key
     */
    private String getRedisKey(String prefix, String value) {
        return REDIS_KEY_PREFIX + prefix + ":" + value;
    }

    /**
     * 生成分布式锁的key
     * @param prefix 前缀标识，如 "id", "vid" 等
     * @param value 具体的值
     * @return 分布式锁的key
     */
    private String getRedisLockKey(String prefix, String value) {
        return REDIS_KEY_PREFIX + "lock:" + prefix + ":" + value;
    }

    @Override
    @Transactional
    public Signal createSignal(Signal signal) {
        log.info("开始创建信号数据: {}", signal);
        if (signal == null) {
            log.error("创建信号数据失败：信号数据为空");
            throw new IllegalArgumentException("信号数据不能为空");
        }
        if (signal.getVid() == null || signal.getVid().trim().isEmpty()) {
            log.error("创建信号数据失败：车辆VID为空, signal: {}", signal);
            throw new IllegalArgumentException("车辆VID不能为空");
        }

        try {
            // 设置默认值
            signal.setStatus(0);
            signal.setRecordedAt(LocalDateTime.now());
            log.debug("设置信号数据默认值：recordedAt={}, status={}", signal.getRecordedAt(), signal.getStatus());

            // 1. 先写入数据库
            int result = signalMapper.save(signal);
            if (result != 1) {
                log.error("创建信号数据失败：数据库操作影响行数为{}", result);
                throw new RuntimeException("创建信号数据失败");
            }

            // 2. 写入缓存（采用异步方式）
            String redisKey = getRedisKey("id", signal.getSignalId().toString());
            redisTemplate.opsForValue().set(redisKey, signal, getRandomExpireTime(), TimeUnit.SECONDS);
            
            // 3. 删除对应VID的缓存，保证数据一致性
            String vidRedisKey = getRedisKey("vid", signal.getVid());
            redisTemplate.delete(vidRedisKey);
            
            log.info("成功创建信号数据: signalId={}, vid={}", signal.getSignalId(), signal.getVid());
            return signal;
        } catch (Exception e) {
            log.error("创建信号失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建信号失败", e);
        }
    }

    @Override
    public Signal getSignalById(Long signalId) {
        log.info("开始查询信号数据, signalId: {}", signalId);
        String redisKey = getRedisKey("id", signalId.toString());
        String lockKey = getRedisLockKey("id", signalId.toString());

        // 1. 先查询缓存
        Signal signal = (Signal) redisTemplate.opsForValue().get(redisKey);
        if (signal != null) {
            log.info("从Redis缓存获取信号数据: signalId={}", signalId);
            return signal;
        }

        // 2. 缓存未命中，使用分布式锁防止缓存击穿
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            // 尝试获取锁
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                try {
                    // 双重检查，防止其他线程已经写入缓存
                    signal = (Signal) redisTemplate.opsForValue().get(redisKey);
                    if (signal != null) {
                        return signal;
                    }

                    // 3. 从数据库查询
                    signal = signalMapper.findById(signalId);
                    if (signal == null) {
                        log.error("查询信号数据失败：未找到ID为{}的信号数据", signalId);
                        throw new RuntimeException("mysql中未找到ID为: " + signalId + " 的信号数据");
                    }

                    // 4. 写入缓存
                    redisTemplate.opsForValue().set(redisKey, signal, getRandomExpireTime(), TimeUnit.SECONDS);
                    log.info("从mysql获取信号数据并写入redis: signalId={}", signalId);
                    return signal;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁超时: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        
        // 如果获取锁失败，直接从数据库读取
        return signalMapper.findById(signalId);
    }

    @Override
    @Transactional
    public Signal updateSignal(Signal signal) {
        log.info("开始更新信号数据: {}", signal);
        if (signal == null || signal.getSignalId() == null) {
            log.error("更新信号数据失败：信号数据ID为空");
            throw new IllegalArgumentException("信号数据ID不能为空");
        }

        String redisKey = getRedisKey("id", signal.getSignalId().toString());
        String lockKey = getRedisLockKey("id", signal.getSignalId().toString());
        Lock lock = redisLockRegistry.obtain(lockKey);

        try {
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                try {
                    // 1. 检查信号是否存在
                    Signal existingSignal = signalMapper.findById(signal.getSignalId());
                    if (existingSignal == null) {
                        log.error("更新信号数据失败：未找到ID为{}的信号数据", signal.getSignalId());
                        throw new RuntimeException("未找到ID为: " + signal.getSignalId() + " 的信号数据");
                    }

                    // 2. 更新数据库
                    existingSignal.setMaxVoltage(signal.getMaxVoltage());
                    existingSignal.setMinVoltage(signal.getMinVoltage());
                    existingSignal.setMaxCurrent(signal.getMaxCurrent());
                    existingSignal.setMinCurrent(signal.getMinCurrent());
                    existingSignal.setRecordedAt(LocalDateTime.now());

                    int result = signalMapper.update(existingSignal);
                    if (result != 1) {
                        log.error("更新信号数据失败：数据库操作影响行数为{}", result);
                        throw new RuntimeException("更新信号数据失败");
                    }

                    // 3. 删除缓存
                    redisTemplate.delete(redisKey);
                    log.info("成功更新信号数据并删除缓存: signalId={}", existingSignal.getSignalId());
                    return existingSignal;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁超时: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
        }

        throw new RuntimeException("更新信号数据失败：无法获取锁");
    }

    @Override
    @Transactional(timeout = 5)
    public void deleteSignal(Long signalId) {
        log.info("开始删除信号数据, signalId: {}", signalId);
        String redisKey = getRedisKey("id", signalId.toString());
        
        try {
            // 1. 检查是否存在
            if (!signalMapper.existsById(signalId)) {
                log.error("删除信号数据失败：未找到ID为{}的信号数据", signalId);
                throw new RuntimeException("未找到ID为: " + signalId + " 的信号数据");
            }

            // 2. 软删除数据库记录
            int result = signalMapper.softDelete(signalId);
            if (result != 1) {
                log.error("删除信号数据失败：数据库操作影响行数为{}", result);
                throw new RuntimeException("删除信号数据失败");
            }

            // 3. 删除缓存
            redisTemplate.delete(redisKey);
            log.info("成功删除信号数据并清除缓存: signalId={}", signalId);
        } catch (Exception e) {
            log.error("删除信号失败: signalId={}, error={}", signalId, e.getMessage(), e);
            throw new RuntimeException("删除信号失败", e);
        }
    }

    @Override
    @Transactional(timeout = 5)
    public void deleteSignalsByVid(String vid) {
        log.info("开始删除车辆信号数据, vid: {}", vid);
        try {
            if (vid == null || vid.trim().isEmpty()) {
                log.error("删除车辆信号数据失败：车辆VID为空");
                throw new IllegalArgumentException("车辆VID不能为空");
            }

            // 1. 查询所有相关信号
            List<Signal> signals = signalMapper.findByVid(vid);
            
            // 2. 软删除数据库记录
            int result = signalMapper.softDeleteByVid(vid);
            if (result < 0) {
                log.error("删除车辆信号数据失败：数据库操作影响行数为{}", result);
                throw new RuntimeException("删除车辆相关信号数据失败");
            }

            // 3. 删除所有相关缓存
            for (Signal signal : signals) {
                String redisKey = getRedisKey("id", signal.getSignalId().toString());
                redisTemplate.delete(redisKey);
            }
            String vidRedisKey = getRedisKey("vid", vid);
            redisTemplate.delete(vidRedisKey);
            
            log.info("成功删除车辆信号数据并清除缓存: vid={}, 删除条数={}", vid, signals.size());
        } catch (Exception e) {
            log.error("删除车辆信号数据时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Signal> getSignalsByVid(String vid) {
        log.info("开始查询车辆信号数据, vid: {}", vid);
        if (vid == null || vid.trim().isEmpty()) {
            log.error("查询车辆信号数据失败：车辆VID为空");
            throw new IllegalArgumentException("车辆VID不能为空");
        }

        String redisKey = getRedisKey("vid", vid);
        String lockKey = getRedisLockKey("vid", vid);

        // 1. 先查询缓存
        List<Signal> signals = (List<Signal>) redisTemplate.opsForValue().get(redisKey);
        if (signals != null) {
            log.info("从Redis缓存获取车辆信号数据: vid={}, 数据条数={}", vid, signals.size());
            return signals;
        }

        // 2. 缓存未命中，使用分布式锁防止缓存击穿
        Lock lock = redisLockRegistry.obtain(lockKey);
        try {
            // 尝试获取锁
            if (lock.tryLock(LOCK_TIMEOUT, TimeUnit.SECONDS)) {
                try {
                    // 双重检查，防止其他线程已经写入缓存
                    signals = (List<Signal>) redisTemplate.opsForValue().get(redisKey);
                    if (signals != null) {
                        return signals;
                    }

                    // 3. 从数据库查询
                    signals = signalMapper.findByVid(vid);
                    if (signals == null) {
                        signals = new ArrayList<>();
                    }

                    // 4. 写入缓存
                    redisTemplate.opsForValue().set(redisKey, signals, getRandomExpireTime(), TimeUnit.SECONDS);
                    log.info("从mysql获取车辆信号数据并写入redis: vid={}, 数据条数={}", vid, signals.size());
                    return signals;
                } finally {
                    lock.unlock();
                }
            } else {
                log.warn("获取分布式锁超时，直接从数据库读取: vid={}", vid);
                return signalMapper.findByVid(vid);
            }
        } catch (InterruptedException e) {
            log.error("获取分布式锁异常: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            return signalMapper.findByVid(vid);
        } catch (Exception e) {
            log.error("查询车辆信号数据时发生错误: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Signal> getAllSignals() {
        try {
            return signalMapper.getAllSignals();
        } catch (Exception e) {
            log.error("获取所有信号失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取所有信号失败", e);
        }
    }

    @Override
    public boolean existsSignal(Long signalId) {
        log.debug("检查信号数据是否存在, signalId: {}", signalId);
        String redisKey = getRedisKey("id", signalId.toString());
        
        // 1. 先查询缓存
        Boolean exists = redisTemplate.hasKey(redisKey);
        if (Boolean.TRUE.equals(exists)) {
            log.debug("从缓存中确认信号数据存在: signalId={}", signalId);
            return true;
        }
        
        // 2. 查询数据库
        boolean existsInDb = signalMapper.existsById(signalId);
        log.debug("信号数据存在检查结果: signalId={}, exists={}", signalId, existsInDb);
        return existsInDb;
    }
}
