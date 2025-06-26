package com.xiaomi.warn.mapper;

import com.xiaomi.warn.entity.BatteryType;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface batMapper {
    /**
     * 插入新的电池类型
     * @param batteryType 电池类型信息
     * @return 影响的行数
     */
    @Insert("INSERT INTO battery_type (type_name, is_deleted) VALUES (#{typeName}, #{isDeleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(BatteryType batteryType);

    /**
     * 根据ID查询电池类型
     * @param id 电池类型ID
     * @return 电池类型信息
     */
    @Select("SELECT id, type_name as typeName, is_deleted as isDeleted FROM battery_type WHERE id = #{id}")
    BatteryType selectById(@Param("id") int id);

    /**
     * 查询所有未删除的电池类型
     * @return 电池类型列表
     */
    @Select("SELECT id, type_name as typeName, is_deleted as isDeleted FROM battery_type WHERE is_deleted = false")
    List<BatteryType> selectAll();

    /**
     * 更新电池类型信息
     * @param batteryType 更新的电池类型信息
     * @return 影响的行数
     */
    @Update("UPDATE battery_type SET type_name = #{typeName}, is_deleted = #{isDeleted} WHERE id = #{id}")
    int update(BatteryType batteryType);

    /**
     * 软删除电池类型
     * @param id 电池类型ID
     * @return 影响的行数
     */
    @Update("UPDATE battery_type SET is_deleted = true WHERE id = #{id}")
    int softDelete(@Param("id") int id);

    /**
     * 检查电池类型是否存在且未删除
     * @param id 电池类型ID
     * @return 是否存在且未删除
     */
    @Select("SELECT COUNT(1) > 0 FROM battery_type WHERE id = #{id} AND is_deleted = false")
    boolean existsAndNotDeleted(@Param("id") int id);

    /**
     * 根据类型名称查询未删除的电池类型
     * @param typeName 类型名称
     * @return 电池类型信息
     */
    @Select("SELECT id, type_name as typeName, is_deleted as isDeleted FROM battery_type WHERE type_name = #{typeName} AND is_deleted = false")
    BatteryType selectByTypeName(@Param("typeName") String typeName);
}
