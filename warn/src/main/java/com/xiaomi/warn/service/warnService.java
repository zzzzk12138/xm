package com.xiaomi.warn.service;

import com.xiaomi.warn.dto.warnDTO;
import com.xiaomi.warn.entity.WarnRule;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface warnService {
    /**
     * 批量处理警告信息
     * @param warnDTOList 警告信息列表
     * @return 处理结果列表
     */
    List<Map<String, Object>> processWarns(List<warnDTO> warnDTOList);

    /**
     * 查询指定车辆的预警信息
     * @param carId 车辆ID
     * @return 预警信息列表
     */
    List<Map<String, Object>> getWarnsByCarId(Integer carId);
}
