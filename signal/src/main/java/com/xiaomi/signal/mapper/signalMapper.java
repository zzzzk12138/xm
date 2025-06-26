package com.xiaomi.signal.mapper;

import com.xiaomi.signal.entity.Signal;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Set;

@Mapper
public interface signalMapper {
    // 保存信号数据
    int save(Signal signal);
    
    // 根据信号ID查找
    Signal findById(Long signalId);
    
    // 根据车辆VID查找所有信号
    List<Signal> findByVid(String vid);
    
    // 查找所有未删除的信号
    List<Signal> getAllSignals();
    
    // 查找所有信号
    List<Signal> findAll();
    
    // 更新信号数据
    int update(Signal signal);
    
    // 软删除信号
    int softDelete(Long signalId);
    
    // 检查信号是否存在
    boolean existsById(Long signalId);
    
    // 根据VID软删除信号
    int softDeleteByVid(String vid);

    // 根据VID列表批量更新信号状态
    int updateSignalStatusByVids(@Param("vids") Set<String> vids);
}
