package com.jinjiaxin.yixiapan.entity.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis配置类
 *
 */
@Configuration
@Slf4j
public class RedisConfig<v> {

    /**
     * springboot2.x 使用LettuceConnectionFactory 代替 RedisConnectionFactory
     * application.yml配置基本信息后,springboot2.x  RedisAutoConfiguration能够自动装配
     * LettuceConnectionFactory 和 RedisConnectionFactory 及其 RedisTemplate
     *
     * @param factory
     * @return
     */
    @Bean
    public RedisTemplate<String,v> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String,v> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(RedisSerializer.json());
        redisTemplate.setHashKeySerializer(RedisSerializer.string());
        redisTemplate.setHashValueSerializer(RedisSerializer.json());
        redisTemplate.setConnectionFactory(factory);
        log.info("redisTemplate：" + redisTemplate.toString());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
