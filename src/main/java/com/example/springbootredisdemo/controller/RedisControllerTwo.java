package com.example.springbootredisdemo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @version 0.0.1
 * @program: springbootredisdemo
 * @description: redis控制层
 * @packname: com.example.springbootredisdemo.controller
 * @author: wzp
 * @create: 2019-11-18 08:51
 */
@RestController
public class RedisControllerTwo {
    /**
     * 此处引入了StringRedisTemplate类;如果有必要了解可以去网上搜查
     * https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/StringRedisTemplate.html
     */
    @Autowired
    private StringRedisTemplate template;

    /***
     * 获取值
     * @param key
     * @return
     */
    @RequestMapping("/redis/get/{key}")
    public String get(@PathVariable("key") String key){
        return template.opsForValue().get(key);
    }
    @RequestMapping("/redis/set/{key}/{value}")
    public Boolean set(@PathVariable("key") String key,@PathVariable("value") String value){
        Boolean flag = true;
        try{
            template.opsForValue().set(key,value);
        }catch (Exception e){
            e.printStackTrace();
            flag = false;
        }

        return  flag;
    }
}
