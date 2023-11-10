package com.imooc.bilibili.api;


import com.imooc.bilibili.service.DemoService;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;

@SpringBootTest
public class ImoocBilibiliAppTests {
    @Autowired
    DemoService demoService;

    @Test
    @GetMapping("/query")
    public Long query(Long id) {
        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        return demoService.query(id);
    }
}
