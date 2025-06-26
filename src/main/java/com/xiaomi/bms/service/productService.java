package com.xiaomi.bms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class productService {

    @Autowired
    private com.xiaomi.bms.mapper.productMapper productMapper;

    public Long getStockById(Integer id) {
        return productMapper.getstockById(id);
    }
}
