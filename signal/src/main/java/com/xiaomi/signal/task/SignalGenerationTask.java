package com.xiaomi.signal.task;

import com.xiaomi.signal.entity.Signal;
import com.xiaomi.signal.service.signalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Slf4j
@Component
public class SignalGenerationTask {

    @Autowired
    private signalService signalService;

    // 测试用的车辆VID列表
    private static final String[] TEST_VIDS = {
            "VH6FTQ5CHDZOEXEI",
            "VHIKR4B9KO8O3OCT",
            "VHL3C6TDUXFM6HV6",
            "VHIF05TZ1WMONZ52",
            "VHOXMWGL8MJ73MCL"
    };

    private final Random random = new Random();

    // 电流报警等级阈值
    private static final BigDecimal CURRENT_ALERT_LEVEL_0 = new BigDecimal("3.000"); // 差值 >= 3A
    private static final BigDecimal CURRENT_ALERT_LEVEL_1 = new BigDecimal("1.000"); // 1A <= 差值 < 3A
    private static final BigDecimal CURRENT_ALERT_LEVEL_2 = new BigDecimal("0.200"); // 0.2A <= 差值 < 1A
    // 差值 < 0.2A 不报警

    // 电压报警等级阈值（高电压规则：>400V）
    private static final BigDecimal VOLTAGE_HIGH_ALERT_LEVEL_0 = new BigDecimal("5.000"); // 差值 >= 5V
    private static final BigDecimal VOLTAGE_HIGH_ALERT_LEVEL_1 = new BigDecimal("3.000"); // 3V <= 差值 < 5V
    private static final BigDecimal VOLTAGE_HIGH_ALERT_LEVEL_2 = new BigDecimal("1.000"); // 1V <= 差值 < 3V
    private static final BigDecimal VOLTAGE_HIGH_ALERT_LEVEL_3 = new BigDecimal("0.600"); // 0.6V <= 差值 < 1V
    private static final BigDecimal VOLTAGE_HIGH_ALERT_LEVEL_4 = new BigDecimal("0.200"); // 0.2V <= 差值 < 0.6V

    // 电压报警等级阈值（低电压规则：<=400V）
    private static final BigDecimal VOLTAGE_LOW_ALERT_LEVEL_0 = new BigDecimal("2.000"); // 差值 >= 2V
    private static final BigDecimal VOLTAGE_LOW_ALERT_LEVEL_1 = new BigDecimal("1.000"); // 1V <= 差值 < 2V
    private static final BigDecimal VOLTAGE_LOW_ALERT_LEVEL_2 = new BigDecimal("0.700"); // 0.7V <= 差值 < 1V
    private static final BigDecimal VOLTAGE_LOW_ALERT_LEVEL_3 = new BigDecimal("0.400"); // 0.4V <= 差值 < 0.7V
    private static final BigDecimal VOLTAGE_LOW_ALERT_LEVEL_4 = new BigDecimal("0.200"); // 0.2V <= 差值 < 0.4V

    private static final BigDecimal VOLTAGE_THRESHOLD = new BigDecimal("400.000"); // 电压阈值

    /**
     * 每10秒执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0/10 * * * * ?")
    public void generateSignalData() {
        log.info("每隔10s定时生成signal数据");
        try {
            // 随机选择一个VID
            String vid = TEST_VIDS[random.nextInt(TEST_VIDS.length)];
            
            // 生成随机电压基准值（360V-420V之间）
            BigDecimal baseVoltage = new BigDecimal("360.000").add(
                    new BigDecimal(random.nextDouble() * 60)
                            .setScale(3, RoundingMode.HALF_UP)
            );

            // 根据基准电压选择报警规则并生成电压差值
            BigDecimal[] voltageValues = generateVoltageValues(baseVoltage);
            BigDecimal maxVoltage = voltageValues[0];
            BigDecimal minVoltage = voltageValues[1];
            
            // 生成随机电流值和波动
            BigDecimal[] currentValues = generateCurrentValues();
            BigDecimal maxCurrent = currentValues[0];
            BigDecimal minCurrent = currentValues[1];

            // 创建信号对象
            Signal signal = new Signal();
            signal.setVid(vid);
            signal.setMaxVoltage(maxVoltage);
            signal.setMinVoltage(minVoltage);
            signal.setMaxCurrent(maxCurrent);
            signal.setMinCurrent(minCurrent);

            // 保存信号数据
            signalService.createSignal(signal);
            
            // 计算差值和报警等级
            BigDecimal voltageDiff = maxVoltage.subtract(minVoltage);
            BigDecimal currentDiff = maxCurrent.subtract(minCurrent);
            int voltageAlertLevel = calculateVoltageAlertLevel(voltageDiff, baseVoltage);
            int currentAlertLevel = calculateCurrentAlertLevel(currentDiff);
            
            log.info("成功生成信号数据: vid={}, baseVoltage={}, maxVoltage={}, minVoltage={}, voltageDiff={}, voltageAlertLevel={}, " +
                            "maxCurrent={}, minCurrent={}, currentDiff={}, currentAlertLevel={}",
                    vid, baseVoltage, maxVoltage, minVoltage, voltageDiff, voltageAlertLevel,
                    maxCurrent, minCurrent, currentDiff, currentAlertLevel);
            
        } catch (Exception e) {
            log.error("生成信号数据失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 生成电压最大值和最小值
     */
    private BigDecimal[] generateVoltageValues(BigDecimal baseVoltage) {
        boolean isHighVoltage = baseVoltage.compareTo(VOLTAGE_THRESHOLD) > 0;
        
        // 随机选择报警等级
        int randomAlertType = random.nextInt(6); // 0-5，包括不报警的情况
        BigDecimal voltageDiff;
        
        if (isHighVoltage) {
            // 高电压规则
            switch (randomAlertType) {
                case 0: // 报警等级0：差值>=5V
                    voltageDiff = VOLTAGE_HIGH_ALERT_LEVEL_0.add(
                            new BigDecimal(random.nextDouble() * 2) // 再加0-2V的随机值
                    );
                    break;
                case 1: // 报警等级1：3V<=差值<5V
                    voltageDiff = VOLTAGE_HIGH_ALERT_LEVEL_1.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_HIGH_ALERT_LEVEL_0.subtract(VOLTAGE_HIGH_ALERT_LEVEL_1).doubleValue())
                    );
                    break;
                case 2: // 报警等级2：1V<=差值<3V
                    voltageDiff = VOLTAGE_HIGH_ALERT_LEVEL_2.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_HIGH_ALERT_LEVEL_1.subtract(VOLTAGE_HIGH_ALERT_LEVEL_2).doubleValue())
                    );
                    break;
                case 3: // 报警等级3：0.6V<=差值<1V
                    voltageDiff = VOLTAGE_HIGH_ALERT_LEVEL_3.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_HIGH_ALERT_LEVEL_2.subtract(VOLTAGE_HIGH_ALERT_LEVEL_3).doubleValue())
                    );
                    break;
                case 4: // 报警等级4：0.2V<=差值<0.6V
                    voltageDiff = VOLTAGE_HIGH_ALERT_LEVEL_4.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_HIGH_ALERT_LEVEL_3.subtract(VOLTAGE_HIGH_ALERT_LEVEL_4).doubleValue())
                    );
                    break;
                default: // 不报警：差值<0.2V
                    voltageDiff = new BigDecimal(random.nextDouble() * 0.2)
                            .setScale(3, RoundingMode.HALF_UP);
            }
        } else {
            // 低电压规则
            switch (randomAlertType) {
                case 0: // 报警等级0：差值>=2V
                    voltageDiff = VOLTAGE_LOW_ALERT_LEVEL_0.add(
                            new BigDecimal(random.nextDouble() * 1) // 再加0-1V的随机值
                    );
                    break;
                case 1: // 报警等级1：1V<=差值<2V
                    voltageDiff = VOLTAGE_LOW_ALERT_LEVEL_1.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_LOW_ALERT_LEVEL_0.subtract(VOLTAGE_LOW_ALERT_LEVEL_1).doubleValue())
                    );
                    break;
                case 2: // 报警等级2：0.7V<=差值<1V
                    voltageDiff = VOLTAGE_LOW_ALERT_LEVEL_2.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_LOW_ALERT_LEVEL_1.subtract(VOLTAGE_LOW_ALERT_LEVEL_2).doubleValue())
                    );
                    break;
                case 3: // 报警等级3：0.4V<=差值<0.7V
                    voltageDiff = VOLTAGE_LOW_ALERT_LEVEL_3.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_LOW_ALERT_LEVEL_2.subtract(VOLTAGE_LOW_ALERT_LEVEL_3).doubleValue())
                    );
                    break;
                case 4: // 报警等级4：0.2V<=差值<0.4V
                    voltageDiff = VOLTAGE_LOW_ALERT_LEVEL_4.add(
                            new BigDecimal(random.nextDouble() * 
                                    VOLTAGE_LOW_ALERT_LEVEL_3.subtract(VOLTAGE_LOW_ALERT_LEVEL_4).doubleValue())
                    );
                    break;
                default: // 不报警：差值<0.2V
                    voltageDiff = new BigDecimal(random.nextDouble() * 0.2)
                            .setScale(3, RoundingMode.HALF_UP);
            }
        }
        
        // 将差值平均分配到最大值和最小值
        BigDecimal halfDiff = voltageDiff.divide(new BigDecimal("2"), 3, RoundingMode.HALF_UP);
        BigDecimal maxVoltage = baseVoltage.add(halfDiff);
        BigDecimal minVoltage = baseVoltage.subtract(halfDiff);
        
        return new BigDecimal[]{maxVoltage, minVoltage};
    }

    /**
     * 计算电压报警等级
     */
    private int calculateVoltageAlertLevel(BigDecimal voltageDiff, BigDecimal baseVoltage) {
        boolean isHighVoltage = baseVoltage.compareTo(VOLTAGE_THRESHOLD) > 0;
        
        if (isHighVoltage) {
            // 高电压规则
            if (voltageDiff.compareTo(VOLTAGE_HIGH_ALERT_LEVEL_0) >= 0) {
                return 0;
            } else if (voltageDiff.compareTo(VOLTAGE_HIGH_ALERT_LEVEL_1) >= 0) {
                return 1;
            } else if (voltageDiff.compareTo(VOLTAGE_HIGH_ALERT_LEVEL_2) >= 0) {
                return 2;
            } else if (voltageDiff.compareTo(VOLTAGE_HIGH_ALERT_LEVEL_3) >= 0) {
                return 3;
            } else if (voltageDiff.compareTo(VOLTAGE_HIGH_ALERT_LEVEL_4) >= 0) {
                return 4;
            }
        } else {
            // 低电压规则
            if (voltageDiff.compareTo(VOLTAGE_LOW_ALERT_LEVEL_0) >= 0) {
                return 0;
            } else if (voltageDiff.compareTo(VOLTAGE_LOW_ALERT_LEVEL_1) >= 0) {
                return 1;
            } else if (voltageDiff.compareTo(VOLTAGE_LOW_ALERT_LEVEL_2) >= 0) {
                return 2;
            } else if (voltageDiff.compareTo(VOLTAGE_LOW_ALERT_LEVEL_3) >= 0) {
                return 3;
            } else if (voltageDiff.compareTo(VOLTAGE_LOW_ALERT_LEVEL_4) >= 0) {
                return 4;
            }
        }
        return -1; // 不报警
    }

    /**
     * 生成电流最大值和最小值
     * @return [最大电流, 最小电流]
     */
    private BigDecimal[] generateCurrentValues() {
        // 基础电流20A
        BigDecimal baseCurrent = new BigDecimal("20.000");
        
        // 随机选择报警等级
        int randomAlertType = random.nextInt(4); // 0-3，包括不报警的情况
        BigDecimal currentDiff;
        
        switch (randomAlertType) {
            case 0: // 报警等级0：差值>=3A
                currentDiff = CURRENT_ALERT_LEVEL_0.add(
                        new BigDecimal(random.nextDouble() * 2) // 再加0-2A的随机值
                );
                break;
            case 1: // 报警等级1：1A<=差值<3A
                currentDiff = CURRENT_ALERT_LEVEL_1.add(
                        new BigDecimal(random.nextDouble() * 
                                CURRENT_ALERT_LEVEL_0.subtract(CURRENT_ALERT_LEVEL_1).doubleValue())
                );
                break;
            case 2: // 报警等级2：0.2A<=差值<1A
                currentDiff = CURRENT_ALERT_LEVEL_2.add(
                        new BigDecimal(random.nextDouble() * 
                                CURRENT_ALERT_LEVEL_1.subtract(CURRENT_ALERT_LEVEL_2).doubleValue())
                );
                break;
            default: // 不报警：差值<0.2A
                currentDiff = new BigDecimal(random.nextDouble() * 0.2)
                        .setScale(3, RoundingMode.HALF_UP);
        }
        
        // 将差值平均分配到最大值和最小值
        BigDecimal halfDiff = currentDiff.divide(new BigDecimal("2"), 3, RoundingMode.HALF_UP);
        BigDecimal maxCurrent = baseCurrent.add(halfDiff);
        BigDecimal minCurrent = baseCurrent.subtract(halfDiff);
        
        return new BigDecimal[]{maxCurrent, minCurrent};
    }

    /**
     * 计算报警等级
     * @param currentDiff 电流差值
     * @return 报警等级（0-最严重，2-最轻微，-1-不报警）
     */
    private int calculateCurrentAlertLevel(BigDecimal currentDiff) {
        if (currentDiff.compareTo(CURRENT_ALERT_LEVEL_0) >= 0) {
            return 0;
        } else if (currentDiff.compareTo(CURRENT_ALERT_LEVEL_1) >= 0) {
            return 1;
        } else if (currentDiff.compareTo(CURRENT_ALERT_LEVEL_2) >= 0) {
            return 2;
        } else {
            return -1; // 不报警
        }
    }
} 