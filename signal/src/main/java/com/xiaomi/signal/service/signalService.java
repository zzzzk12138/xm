package com.xiaomi.signal.service;

import com.xiaomi.signal.entity.Signal;
import java.util.List;

public interface signalService {
    // 创建信号记录
    Signal createSignal(Signal signal);
    
    // 根据信号ID获取信号
    Signal getSignalById(Long signalId);
    
    // 根据车辆VID获取信号列表
    List<Signal> getSignalsByVid(String vid);
    
    // 获取所有信号
    List<Signal> getAllSignals();
    
    // 更新信号信息
    Signal updateSignal(Signal signal);
    
    // 删除信号
    void deleteSignal(Long signalId);
    
    // 根据VID删除信号
    void deleteSignalsByVid(String vid);
    
    // 检查信号是否存在
    boolean existsSignal(Long signalId);
}
