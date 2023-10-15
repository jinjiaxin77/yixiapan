package com.jinjiaxin.yixiapan.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisUtils<v> {

    @Autowired
    private RedisTemplate<String,v> redisTemplate;

    /**
     * 删除缓存
     *
     * @param key
     */
    public void delete(String... key){
        if(key != null && key.length > 0){
            if(key.length == 1){
                redisTemplate.delete(key[0]);
            }else{
                redisTemplate.delete((Collection<String>) CollectionUtils.arrayToList(key));
            }
        }
    }

    public v get(String key){
        return key == null? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 普通缓存放入
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, v value){
        try{
            redisTemplate.opsForValue().set(key,value);
            return true;
        }catch (Exception e){
            log.error("设置redisKey:{},value:{}失败",key,value);
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key
     * @param value
     * @param time
     * @return
     */
    public boolean setex(String key, v value, long time){
        try{
            if(time > 0){
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            }else{
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        }catch (Exception e){
            log.error("设置redisKey:{},value:{}失败",key,value);
            return false;
        }
    }

}
