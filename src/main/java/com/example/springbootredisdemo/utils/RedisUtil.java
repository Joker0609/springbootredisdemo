package com.example.springbootredisdemo.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @version 0.0.1
 * @program: springbootredisdemo
 * @description: redis工具类集成的五种数据类型
 * @packname: com.example.springbootredisdemo.utils
 * @author: wzp
 * @create: 2019-11-18 09:10
 */
@SuppressWarnings("All")
@Component
public class RedisUtil {
    private static StringRedisTemplate template;
    /**
     * 静态注入
     */
    public RedisUtil(StringRedisTemplate template){
        RedisUtil.template = template;
    }

    /**
     * 指定缓存失效时间
     * @param key
     * @param time
     */
    public void expire(String key,Long time){
        try{
            if(time > 0){
                template.expire(key,time, TimeUnit.SECONDS);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 根据key获取过期时间
     * @param key 键,不能为null
     * @return 时间为秒,如果返回为0则代表永远有效
     */
    public long getExpire(String key){
        return template.getExpire(key,TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return 存在true,不存在false
     */
    public boolean hasKey(String key){
        try{
            return template.hasKey(key);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除缓存
     * @param key key可以是一个,也可以是多个
     */
    public void del(String... key){
        if(key!= null && key.length>0){
            if(key.length == 1){
                template.delete(key[0]);
            }else{
                template.delete(CollectionUtils.arrayToList(key));
            }
        }
    }
    /**
     * 支持的数据类型
     */
    /**
     * 1.String
     */
    /**
     *  普通缓存获取
     * @param key
     * @return 返回key对应值
     */
    public Object get(String key) {
        return key == null ? null : template.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     * @param key 键
     * @param value 值
     * @return success:true;false:false
     */
    public boolean set(String key, Object value) {
        try {
            template.opsForValue().set(key, String.valueOf(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     * @param key
     * @param value
     * @param time 时间（秒）;time要大于0,如果time<0将设置无限期
     * @return success:true;false:false
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                template.opsForValue().set(key, String.valueOf(value), time, TimeUnit.SECONDS);
            } else {
                set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 递增方法
     * @param key
     * @param delta
     * @return
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return template.opsForValue().increment(key, delta);
    }

    /**
     * 递减
     * @param key
     * @param delta
     * @return
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return template.opsForValue().increment(key, -delta);
    }
    // ================map===============

    /**
     * HashGet
     * @param key 键,不能为null
     * @param item 项,不能为null
     * @return 值
     */
    public Object hget(String key, String item) {
        return template.opsForHash().get(key, item);
    }

    /**
     * 获取hashkey对应的所有键值
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hmget(String key) {
        return template.opsForHash().entries(key);
    }

    /**
     * HashSet
     * @param key 键
     * @param map 对应多个键值
     * @return 成功:true;失败:false;
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            template.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * HashSet 并设置时间
     * @param key 键
     * @param map 对应多个键值
     * @param time 时间（秒）
     * @return 成功true;失败false
     */
    public boolean hmset(String key, Map<String, Object> map, long time) {
        try {
            template.opsForHash().putAll(key, map);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     *  向同一张hash表中放入数据,如果不存在将创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @return true成功,false失败
     */
    public boolean hset(String key, String item, Object value) {
        try {
            template.opsForHash().put(key, item, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 向一张hash表中放入数据,如果不存在创建
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
     * @return true：成功 false：失败
     */
    public boolean hset(String key, String item, Object value, long time) {
        try {
            template.opsForHash().put(key, item, value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除hash表中的值
     * @param key 键:不能为null
     * @param item 项:可以是多个 不能为null
     */
    public void hdel(String key, Object... item) {
        template.opsForHash().delete(key, item);
    }

    /**
     * 判断hash表中是否有该项的值
     * @param key
     * @param item
     * @return 存在true;不存在false
     */
    public boolean hHasKey(String key, String item) {
        return template.opsForHash().hasKey(key, item);
    }

    /**
     * hash递增,如果不存在,就会创建一个,并把新增的值返回
     * @param key 键
     * @param item 项
     * @param by 要增加几（大于0）
     * @return
     */
    public double hincr(String key, String item, double by) {
        return template.opsForHash().increment(key, item, by);
    }

    /**
     * hash 递减
     * @param key 键
     * @param item 项
     * @param by 要递减几（小于0）
     * @return
     */
    public double hdecr(String key, String item, double by) {
        return template.opsForHash().increment(key, item, -by);
    }
    //============================Set=============================

    /**
     * 根据key获取Set中的所有值
     *
     * @param key 键
     * @return
     */
    public Set<Object> sGet(String key) {
        try {
            return Collections.singleton(template.opsForSet().members(key));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据value从一个set中查询,是否存在
     *
     * @param key  键
     * @param value 值
     * @return true 存在 false不存在
     */
    public boolean sHasKey(String key, Object value) {
        try {
            return template.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将数据放入set缓存
     *
     * @param key  键
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSet(String key, Object... values) {
        try {
            return template.opsForSet().add(key, String.valueOf(values));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 将set数据放入缓存
     *
     * @param key  键
     * @param time  时间(秒)
     * @param values 值 可以是多个
     * @return 成功个数
     */
    public long sSetAndTime(String key, long time, String... values) {
        try {
            Long count = template.opsForSet().add(key, values);
            if (time > 0) {
                expire(key, time);
            }
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取set缓存的长度
     *
     * @param key 键
     * @return
     */
    public long sGetSetSize(String key) {
        try {
            return template.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 移除值为value的
     *
     * @param key  键
     * @param values 值 可以是多个
     * @return 移除的个数
     */
    public long setRemove(String key, Object... values) {
        try {
            Long count = template.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    //===============================list=================================

    /**
     * 获取list缓存的内容
     *
     * @param key  键
     * @param start 开始
     * @param end  结束 0 到 -1代表所有值
     * @return
     */
    public List<String> lGet(String key, long start, long end) {
        try {
            return template.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取list缓存的长度
     *
     * @param key 键
     * @return
     */
    public long lGetListSize(String key) {
        try {
            return template.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 通过索引 获取list中的值
     *
     * @param key  键
     * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
     * @return
     */
    public Object lGetIndex(String key, long index) {
        try {
            return template.opsForList().index(key, index);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key  键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, Object value) {
        try {
            template.opsForList().rightPush(key, (String) value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key  键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, Object value, long time) {
        try {
            template.opsForList().rightPush(key, (String) value);
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key  键
     * @param value 值
     * @return
     */
    public boolean lSet(String key, List<Object> value) {
        try {
            template.opsForList().rightPushAll(key, String.valueOf(value));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 将list放入缓存
     *
     * @param key  键
     * @param value 值
     * @param time 时间(秒)
     * @return
     */
    public boolean lSet(String key, List<Object> value, long time) {
        try {
            template.opsForList().rightPushAll(key, String.valueOf(value));
            if (time > 0) {
                expire(key, time);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据索引修改list中的某条数据
     *
     * @param key  键
     * @param index 索引
     * @param value 值
     * @return
     */
    public boolean lUpdateIndex(String key, long index, Object value) {
        try {
            template.opsForList().set(key, index, (String) value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 移除N个值为value
     *
     * @param key  键
     * @param count 移除多少个
     * @param value 值
     * @return 移除的个数
     */
    public long lRemove(String key, long count, Object value) {
        try {
            Long remove = template.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
