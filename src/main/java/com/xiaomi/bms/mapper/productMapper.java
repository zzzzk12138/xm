package com.xiaomi.bms.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface productMapper {


    @Select("select stock from product where id = #{id}")
    Long getstockById(Integer id);
}
