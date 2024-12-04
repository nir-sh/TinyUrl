package com.handson.tinyurl.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Lazy
@Component
public class Redis {

    @Autowired
    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    //============================= Common ============================
    /**
     * Set the cache expiration time
     * @param key Key
     * @param time Time (in seconds)
     * @return true if successful, false otherwise
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get the expiration time of a key
     * @param key Key (cannot be null)
     * @return Time in seconds (returns 0 if the key is permanent)
     */
    public long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * Check if a key exists
     * @param key Key
     * @return true if exists, false otherwise
     */
    public boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete cache for one or multiple keys
     * @param key Single or multiple keys
     */
    @SuppressWarnings("unchecked")
    public void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    //============================ String ============================
    /**
     * Get a value from cache
     * @param key Key
     * @return Value
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * Set a value in cache
     * @param key Key
     * @param value Value
     * @return true if successful, false otherwise
     */
    public boolean set(String key, Object value) {
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Set a value in cache with expiration time
     * @param key Key
     * @param value Value
     * @param time Expiration time (in seconds; set to unlimited if <= 0)
     * @return true if successful, false otherwise
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Increment a value
     * @param key Key
     * @param delta Increment value (>0)
     * @return New value
     */
    public long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Increment factor must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * Decrement a value
     * @param key Key
     * @param delta Decrement value (>0)
     * @return New value
     */
    public long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("Decrement factor must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    //============================= Map ==============================
    /**
     * Get a value from a hash by key and field
     * @param key Key
     * @param item Field
     * @return Value
     */
    public Object hget(String key, String item) {
        return redisTemplate.opsForHash().get(key, item);
    }

    /**
     * Get all key-value pairs from a hash
     * @param key Key
     * @return Map of all key-value pairs
     */
    public Map<Object, Object> hmget(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Set multiple key-value pairs in a hash
     * @param key Key
     * @param map Key-value pairs
     * @return true if successful, false otherwise
     */
    public boolean hmset(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Other methods for hashes, sets, and lists continue in a similar manner...
}
