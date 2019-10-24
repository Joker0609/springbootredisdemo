package com.example.springbootredisdemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @version 0.0.1
 * @program: springbootredisdemo
 * @description: redis工具类
 * @packname: com.example.springbootredisdemo.utils
 * @author: wzp
 * @create: 2019-10-24 11:27
 */
@Component
@Slf4j
public class RedisUtils {
    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    @Autowired
    private JedisPool jedisPool;

    /**
     * 通过key获取储存在redis中的value 并释放连接
     *
     * @param key
     * @param indexdb 选择redis库
     * @return 如果成功返回value 失败则为null
     */
    public String get(String key,int indexdb){
        Jedis jedis = null;
        String value = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            value = jedis.get(key);
            log.info(value);
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
        }finally {
            returnResource(jedisPool,jedis);
        }
        return value;
    }

    /**
     * 向redis存入key和value,并释放链接资源
     * 如果key已存在则覆盖
     * @param key
     * @param value
     * @param indexdb 选择redis库 0-15
     * @return 成功返回ok，失败为0
     */
    public String set(String key,String value,int indexdb){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            return jedis.set(key,value);
        }catch (Exception e){
            log.error(e.getMessage());
            return "0";
        }finally {
            returnResource(jedisPool,jedis);
        }
    }



    private void returnResource(JedisPool jedisPool, Jedis jedis) {
    }


}
