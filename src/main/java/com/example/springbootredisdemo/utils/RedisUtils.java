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

    /**
     * 通过key获取存储在redis中的value；此时是通过多个key获取多个value；
     * 并释放连接
     * @param key
     * @param indexdb 选择redis库【0-15】
     * @return 如果成功返回为value；失败则为null；
     */
    public byte[] get(byte[] key, int indexdb){
        Jedis jedis = null;
        byte[] value = null;
        try{
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            value = jedis.get(key);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return value;
    }

    /**
     * 想redis 中存入key和value；多个key多个value；
     * 释放连接
     * 在存入过程中，如果key存在覆盖
     * @param key
     * @param value
     * @param indexdb 选择redis库【0-15】
     * @return 成功返回OK，失败返回0
     */
    public String set(byte[] key, byte[] value,int indexdb){
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

    /**
     * 删除指定key；当然这里也可以是key数组
     * @param keys 这里可以是一个key，也可以是key数组
     * @return 返回删除成功的个数
     */
    public Long del(String... keys){
        Jedis jedis = null;
         try {
             jedis = jedisPool.getResource();
             return jedis.del(keys);
         }catch (Exception e){
             log.error(e.getMessage());
             return 0L;
         }finally {
             returnResource(jedisPool,jedis);
         }
    }
    /**
     * 删除指定的key，也可以传入一个包含key的数组；
     * @param indexdb 选择redis库【0-15】
     * @param keys 一个key或者key数组
     * @return 返回成功的个数
     */
    public Long del(int indexdb,byte[]...keys){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            return jedis.del(keys);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 通过key向指定的value或面追加值
     * @param key
     * @param str
     * @return 成功返回 添加后的value的长度，失败返回添加的value值长度，异常则为0
     */
    public Long append(String key,String str){
        Jedis jedis = null;
        Long res =null;
        try{
            jedis = jedisPool.getResource();
            res = jedis.append(key,str);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     * 判断是否存在key
     * @param key
     * @return true 或者false
     */
    public Boolean exists(String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        }catch (Exception e){
            log.error(e.getMessage());
            return false;
        }finally {
            returnResource(jedisPool, jedis);
        }
    }

    /**
     * 清空当前数据库中的所有key；此命令从不失败；
     * @return 由于此命令从不失败，所以返回OK；
     */
    public String flushDB(){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.flushDB();
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return null;
    }

    private void returnResource(JedisPool jedisPool, Jedis jedis) {
    }


}
