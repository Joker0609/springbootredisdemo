package com.example.springbootredisdemo.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.SortingParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 为给定key 设置生存时间，当key过期时（生命周期时间为0），它会被自动删除
     * @param key
     * @param value 过期时间，单位为秒
     * @param indexdb
     * @return 成功返回1，如果存在和发生异常返回0
     */
    public Long expire(String key,int value,int indexdb){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            return jedis.expire(key,value);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 返回给定key的生存剩余时间；以秒为单位。
     * @param key
     * @param indexdb redis选择库【0-15】
     * @return key不存在时：返回-2，key存在但没有设置剩余生存时间时：返回-1，异常返回0；否则以秒为单位返回key的剩余生存时间
     */
    public Long ttl(String key,int indexdb){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            return jedis.ttl(key);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 移除给定的key的生存时间，将这个key从（易先得）带生存时间的转换成（持久的）一个不带生存时间的key
     * @param key
     * @return 当生存时间移除成功时：返回1；如果key不存在或者没有设置生存时间时：返回0；异常返回-1
     */
    public Long persist(String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.persist(key);
        }catch (Exception e){
            log.error(e.getMessage());
            return -1L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 新增key，并设定生存时间（以秒为单位）
     * @param key
     * @param seconds 生存时间 秒
     * @param value
     * @return 如果成功返回OK，当seconds参数不合法时，返回一个错误。
     */
    public String setex(String key,int seconds, String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.setex(key,seconds,value);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return null;
    }

    /**
     * 设置key，value，并指定这个键值的有效期
     * @param key
     * @param value
     * @param seconds 单位为秒
     * @return 成功返回ok；失败、异常返回null
     */
    public String setex(String key,String value, int seconds){
        Jedis jedis = null;
        String res = null;
        try{
            jedis = jedisPool.getResource();
            res = jedis.setex(key,seconds,value);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }
    /**
     * 设置key，value，如果key已存在,则返回0,
     * @param key
     * @param value
     * @return 成功返回1；失败和异常为0
     */
    public Long setnx(String key, String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.setnx(key,value);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 将指定key的值设置为value,并返回旧值value
     * 当key存在但不是字符串类型时；返回错误
     * @param key
     * @param value
     * @return 返回给定key的旧值,key不存在时返回null；
     */
    public String getSet(String key, String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return jedis.getSet(key,value);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return null;
    }

    /**
     * 通过key 和offset从指定的位置开始将旧value替换
     * 下标是从0开始,offset指从offset的位置开始
     * 如果字符串过小,example： value=bigsea@wzp.cn ;str=abc;offset =7; res = bigsea.abc.cn
     * @param key
     * @param str
     * @param offset
     * @return 返回替换后value的长度
     */
    public Long setrange(String key, int offset, String str){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            return  jedis.setrange(key, offset, str);
        }catch (Exception e){
            log.error(e.getMessage());
            return 0L;
        }finally {
            returnResource(jedisPool,jedis);
        }
    }

    /**
     * 通过批量的key获取批量的value
     * @param keys 在这里是一个String的数组,也可以是一个key
     * @return success: values ;false:null集合;execption:null
     */
    public List<String> mget(String... keys){
        Jedis jedis = null;
        List<String> values = null;
        try {
            jedis = jedisPool.getResource();
            values = jedis.mget(keys);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return values;
    }

    /**
     * 批量的设置key,value ,也可以是一个
     * @param keysvalues
     * example: obj.mset(new String[]{"keys1","value1","keys2","value2"})
     * @return success:ok;false&execption:null
     */
    public String mset(String ... keysvalues){
        Jedis  jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.mset(keysvalues);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     * 批量的设置key和value,可以是一个；
     * 如果key已经存在则会失败，操作回滚；
     * @param keysvalues
     * example: obj.msetnx(new String[]{"keys2","value1","keys2","value2"})
     * @return success
     */
    public Long msetnx(String... keysvalues) {
        Jedis jedis = null;
        Long res = 0L;
        try {
            jedis = jedisPool.getResource();
            res = jedis.msetnx(keysvalues);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 设置key的值,并返回旧值
     * @param key
     * @param value
     * @return success:旧值,如果不存在,则返回null
     */
    public String getset(String key, String value){
        Jedis jedis =null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.getSet(key,value);
        }catch (Exception e){
            log.info(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return  res;
    }
    /**
     * 通过key和下标获取指定下标位置的value
     * @param key
     * @param startOffset 开始位置从0开始,负数表示从右边开始截取
     * @param endOffset
     * @return success:res false：null
     */
    public String getrange(String key, int startOffset, int endOffset){
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.getrange(key, startOffset, endOffset);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     * 通过key 对value进行加1操作,
     * 当value不是int类型时会返回错误,
     * 当key不存在时,value为1
     * @param key
     * @return 加值后的结果
     */
    public Long incr(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.incr(key);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key给指定的value加值,如果key不存在,则参数integer就是value的值
     * @param key
     * @param integer
     * @return
     */
    public Long incrBy(String key, Long integer) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.incrBy(key, integer);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key对value做减减操作,如果key不存在,则value的值为-1
     * @param key
     * @return
     */
    public Long decr(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.decr(key);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key减去指定的值,
     * @param key
     * @param integer
     * @return
     */
    public Long decrBy(String key, Long integer) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.decrBy(key, integer);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     *  通过key获取value的长度
     * @param key
     * @return 返回value的长度
     */
    public Long strlen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.strlen(key);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key给field设置指定的值,如果key不存在,则先创建
     * @param key
     * @param field 字段
     * @param value
     * @return 如果存在返回0,异常返回null
     */
    public Long hset(String key, String field, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hset(key, field, value);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     *  通过key给field设定指定的值,如果key不存在,则先创建,如果field已存在,返回0
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hsetnx(String key,String field, String value){
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hsetnx(key,field,value);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     * 通过key同时设置hash的多个field
     * @param key
     * @param hash
     * @param indexdb
     * @return succes:ok  exception:null
     */
    public String hmset(String key, Map<String, String> hash, int indexdb){
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.hmset(key,hash);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     * 通过key和field获取value值
     * @param key
     * @param field
     * @return success:value 没有返回null
     */
    public String hget(String key, String field) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hget(key, field);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     *  通过key和field获取指定的value, 如果没有对应的value则返回null
     * @param key
     * @param indexdb
     * @param fields 可以是一个String,也可以是一个String数组
     * @return
     */
    public List<String> hmget(String key, int indexdb, String... fields) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.hmget(key, fields);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key给指定的field的value值加上给定的值
     * @param key
     * @param field
     * @param value
     * @return
     */
    public Long hincrby(String key, String field, Long value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hincrBy(key, field, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key和field判断是否有指定的value存在
     * @param key
     * @param field
     * @return
     */
    public Boolean hexists(String key, String field) {
        Jedis jedis = null;
        Boolean res = false;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hexists(key, field);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回field的数量
     * @param key
     * @return
     */
    public Long hlen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hlen(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key 删除指定的field
     * @param key
     * @param fields
     * 可以是一个field,也可以是一个数组
     * @return
     */
    public Long hdel(String key, String... fields) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hdel(key, fields);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回所有的field
     * @param key
     * @return
     */
    public Set<String> hkeys(String key) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hkeys(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回所有和key相关的value
     * @param key
     * @return
     */
    public List<String> hvals(String key) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.hvals(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获得所有的field和value
     * @param key
     * @param indexdb
     * @return
     */
    public Map<String, String> hgetall(String key, int indexdb) {
        Jedis jedis = null;
        Map<String, String> res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.hgetAll(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key向list头部添加字符串
     * @param indexdb
     * @param key
     * @param strs
     * 可以是一个string,也可以是一个string数组
     * @return 返回list中value的个数
     */
    public Long lpush(int indexdb, String key, String... strs) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.lpush(key, strs);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key向list尾部添加字符串
     * @param key
     * @param strs
     * 可以是一个String,也可以是一个string数组
     * @return 返回list中value的个数
     */
    public Long rpush(String key, String... strs) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.rpush(key, strs);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key在list指定的位置之前或之后,添加字符串
     * @param key
     * @param where LIST_POSITION枚举类型
     * @param pivot list里面的的value
     * @param value 添加的value
     * @return
     */
    public Long linsert(String key, BinaryClient.LIST_POSITION where, String pivot,
                        String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.linsert(key, where, pivot, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key设置list指定下标位置的value,如果下标超过list里面value的个数(Indexoutof)
     * @param key
     * @param index 从0开始
     * @param value
     * @return 成功返回OK
     */
    public String lset(String key, Long index, String value) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.lset(key, index, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key从对应的list中删除指定的count个数和value相同的元素
     * @param key
     * @param count 当count为0时删除全部
     * @param value
     * @return 返回被删除的个数
     */
    public Long lrem(String key, long count, String value) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.lrem(key, count, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key保留list从下标开始到end下标结束的value值
     * @param key
     * @param start
     * @param end
     * @return 成功返回ok
     */
    public String ltrim(String key, long start, long end) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.ltrim(key, start, end);
        } catch (Exception e) {

            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }
    /**
     * 通过key从list的头部删除一个value,并返回该value
     * @param key
     * @return
     */
    synchronized public String lpop(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.lpop(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key从list尾部删除一个value,并返回该元素.
     * @param key
     * @param indexdb
     * @return
     */
    synchronized public String rpop(String key, int indexdb) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.rpop(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key从一个list的尾部删除一个value,并添加到另一个value的头部,并返回该value
     * 如果第一个list为空或者不存在时返回null;
     * @param srckey
     * @param dstkey
     * @param indexdb
     * @return
     */
    public String rpoplpush(String srckey, String dstkey, int indexdb) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.rpoplpush(srckey, dstkey);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取list指定下标位置的value
     * 如果没有,返回null
     * @param key
     * @param index
     * @return
     */
    public String lindex(String key, long index) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.lindex(key, index);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回list的长度
     * @param key
     * @return
     */
    public Long llen(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.llen(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取list指定下标位置的value
     * 如果start=0,end=-1,则返回list中全部的value
     * @param key
     * @param start
     * @param end
     * @param indexdb
     * @return
     */
    public List<String> lrange(String key, long start, long end, int indexdb) {
        Jedis jedis = null;
        List<String> res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(indexdb);
            res = jedis.lrange(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 将列表key下标为index的元素的值设为value
     * @param key
     * @param index
     * @param value
     * @return 操作成功返回ok,否则返回错误信息
     */
    public String lset(String key, long index, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lset(key, index, value);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return null;
    }

    /**
     * 返回给定排序后的结果
     * @param key
     * @param sortingParameters
     * @return  返回结果是:列表形式的排序结果
     */
    public List<String> sort(String key, SortingParams sortingParameters) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.sort(key, sortingParameters);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return null;
    }

    /**
     * 返回排序后的结果,排序默认是以数字作为对象,值被解析为双精度的浮点数,然后进行比较.
     * @param key
     * @return 返回结果:排序后的结果
     */
    public List<String> sort(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.sort(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return null;
    }

    /**
     * 通过key向指定的set中添加value
     * @param key
     * @param members 可以是一个String,也可以是一个String数组
     * @return 添加成功的个数
     */
    public Long sadd(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sadd(key, members);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key删除set中对应的值
     * @param key
     * @param members 可以是一个String,也可以是一个String数组.
     * @return 删除的个数
     */
    public Long srem(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.srem(key, members);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key随机删除一个set中的value并返回该value
     * @param key
     * @return
     */
    public String spop(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.spop(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取set中的差集,以第一个set为基准
     * @param keys
     * 可以是一个String,则返回set中所有的value, 也可以是String数组
     * @return
     */
    public Set<String> sdiff(String keys){
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sdiff(keys);
        }catch (Exception e){
            log.error(e.getMessage());
        }finally {
            returnResource(jedisPool,jedis);
        }
        return res;
    }

    /**
     *通过key获取set中的差集并存入到另一个key中.
     * 以第一个set为标准
     * @param dstkey
     * 差集存入的key
     * @param keys
     * 可以是一个String,则返回set中所有的value,也可以是String数组
     * @return
     */
    public Long sdiffstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sdiffstore(dstkey, keys);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取指定中的交集
     * @param keys
     * 可以是一个String,也可以是一个String数组
     * @return
     */
    public Set<String> sinter(String... keys) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sinter(keys);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取指定set中的交集,并将结果存入到新的set中.
     * @param dstkey
     * @param keys
     * 可以是一个String,也可以是一个String数组
     * @return
     */
    public Long sinterstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sinterstore(dstkey, keys);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获得所有set的并集
     * @param keys
     * @return
     */
    public Set<String> sunion(String... keys) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sunion(keys);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回所有set的并集,并存入到新的set中
     * @param dstkey
     * @param keys
     * @return
     */
    public Long sunionstore(String dstkey, String... keys) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sunionstore(dstkey, keys);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key将set中的value移除并添加到第二个set中.
     * @param srckey 需要移除的
     * @param dstkey 需要添加的
     * @param member set中的value
     * @return
     */
    public Long smove(String srckey, String dstkey, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.smove(srckey, dstkey, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取set中value的个数
     * @param key
     * @return
     */
    public Long scard(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.scard(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key判断value是否是set中的元素
     * @param key
     * @param member
     * @return
     */
    public Boolean sismember(String key, String member) {
        Jedis jedis = null;
        Boolean res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.sismember(key, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取set中随机的value,不删除元素.
     * @param key
     * @return
     */
    public String srandmember(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.srandmember(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取set中的所有value
     * @param key
     * @return
     */
    public Set<String> smembers(String key) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.smembers(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key想zset中添加value和score,其中score是用来排序
     * 如果此时value已存在则根据score排序
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Long zadd(String key, double score, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zadd(key, score, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 返回有序集key中,指定区间的成员,min =0,max=-1代表所有元素
     * @param key
     * @param min
     * @param max
     * @return 指定区间内有序集成员的列表
     */
    public Set<String> zrange(String key, long min, long max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrange(key, min, max);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return null;
    }

    /**
     * 统计有序集key中,值在min和max之间的成员数量
     * @param key
     * @param min
     * @param max
     * @return 值在min和max之间成员的数量,异常返回0
     */
    public Long zcount(String key, double min, double max) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcount(key, min, max);
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0L;
        } finally {
            returnResource(jedisPool, jedis);
        }
    }

    /**
     * 为哈希表 key 中的域 field 的值加上增量 increment 。
     * 增量也可以为负数，相当于对给定域进行减法操作。
     * 如果 key不存在，一个新的哈希表被创建并执行 HINCRBY 命令。
     * 如果域 field 不存在，那么在执行命令前，域的值被初始化为 0 。
     * 对一个储存字符串值的域 field 执行 HINCRBY 命令将造成一个错误。
     * 本操作的值被限制在 64 位(bit)有符号数字表示之内。
     *
     * 将名称为key的hash中field的value增加integer
     * @param key
     * @param value
     * @param increment
     * @return 执行hincrBy命令之后,哈希表key中域field的值,异常返回0
     */
    public Long hincrBy(String key, String value, long increment) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.hincrBy(key, value, increment);
        } catch (Exception e) {
            log.error(e.getMessage());
            return 0L;
        } finally {
            returnResource(jedisPool, jedis);
        }
    }

    /**
     * 通过key删除在zset中指定的value
     * @param key
     * @param members
     * @return
     */
    public Long zrem(String key, String... members) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrem(key, members);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key增加该zset中value的score的值
     * @param key
     * @param score
     * @param member
     * @return
     */
    public Double zincrby(String key, double score, String member) {
        Jedis jedis = null;
        Double res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zincrby(key, score, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key 返回zset中value的排名
     * 下标从小到大排序
     * @param key
     * @param member
     * @return
     */
    public Long zrank(String key, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrank(key, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     *通过key返回zset中value的排名
     * 下标从大到小排序
     * @param key
     * @param member
     * @return
     */
    public Long zrevrank(String key, String member) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrevrank(key, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key将获取score从start到end中zset的value
     * score从大到小排序
     * @param key
     * @param start 当start为0,end为-1时返回全部
     * @param end
     * @return
     */
    public Set<String> zrevrange(String key, long start, long end) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回指定score内zsert中的value
     * @param key
     * @param max
     * @param min
     * @return
     */
    public Set<String> zrangebyscore(String key, String max, String min) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrevrangeByScore(key, max, min);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key 返回指定score内zset中的value
     * @param key
     * @param max
     * @param min
     * @return
     */
    public Set<String> zrangeByScore(String key, double max, double min) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zrevrangeByScore(key, max, min);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 返回指定范围内zset中value的数量
     * @param key
     * @param min
     * @param max
     * @return
     */
    public Long zcount(String key, String min, String max) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zcount(key, min, max);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key返回zset中的value的个数
     * @param key
     * @return
     */
    public Long zcard(String key) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zcard(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key获取zset中的value值和score值
     * @param key
     * @param member
     * @return
     */
    public Double zscore(String key, String member) {
        Jedis jedis = null;
        Double res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zscore(key, member);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key删除给定区间的元素
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByRank(String key, long start, long end) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zremrangeByRank(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key删除指定score内的元素
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Long zremrangeByScore(String key, double start, double end) {
        Jedis jedis = null;
        Long res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.zremrangeByScore(key, start, end);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 返回满足pattern表达式的所有key
     * @param pattern
     * @return all keys
     */
    public Set<String> keys(String pattern) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.keys(pattern);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 获取符合pattern的所有key
     * @param pattern
     * @param database
     * @return
     */
    public Set<String> keysBySelect(String pattern,int database) {
        Jedis jedis = null;
        Set<String> res = null;
        try {
            jedis = jedisPool.getResource();
            jedis.select(database);
            res = jedis.keys(pattern);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 通过key判断值得类型
     * @param key
     * @return
     */
    public String type(String key) {
        Jedis jedis = null;
        String res = null;
        try {
            jedis = jedisPool.getResource();
            res = jedis.type(key);
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            returnResource(jedisPool, jedis);
        }
        return res;
    }

    /**
     * 序列化对象
     * 对象需实现serializable接口
     * @param obj
     * @return
     */
    public static byte[] ObjTOSerialize(Object obj) {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream byteOut = null;
        try {
            byteOut = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(byteOut);
            oos.writeObject(obj);
            byte[] bytes = byteOut.toByteArray();
            return bytes;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 反序列化对象
     * @param bytes
     * @return 对象需实现serializable
     */
    public static Object unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        try {
            //反序列化
            bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 返还到连接池
     * @param jedisPool
     * @param jedis
     */
    public static void returnResource(JedisPool jedisPool, Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }
    // public static RedisUtil getRu() {
    // return ru;
    // }
    //
    // public static void setRu(RedisUtil ru) {
    // RedisUtil.ru = ru;
    // }

    public static void main(String[] args) {
		/*JedisPool jedisPool = new JedisPool(null,"localhost",6379,100,"123456");
		Jedis jedis = jedisPool.getResource();
		//r.get("", RedisConstants.datebase4);
		jedis.select(RedisConstants.datebase4);
		Set<String> str =  jedis.keys("*");
		for (String string : str) {
			System.out.println(string);
		}*/
    }


}
