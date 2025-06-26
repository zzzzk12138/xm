package com.xiaomi.warn.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class warnDTO {
    
    /**
     * 车架编号
     * 必须
     */
    @NotNull(message = "车架编号不能为空")
    @JsonProperty("carId")
    private Integer carId;

    /**
     * 规则编号
     * 非必须
     */
    @JsonProperty("warnId")
    private Integer warnId;

    /**
     * 信号
     * 必须，支持以下格式：
     * 1. 电压信号：{"Mx":12.0,"Mi":0.6}
     * 2. 电流信号：{"Ix":12.0,"Ii":11.7}
     * 3. 组合信号：{"Mx":11.0,"Mi":9.6,"Ix":12.0,"Ii":11.7}
     */
    @NotNull(message = "信号不能为空")
    @JsonProperty("signal")
    private String signal;
}
