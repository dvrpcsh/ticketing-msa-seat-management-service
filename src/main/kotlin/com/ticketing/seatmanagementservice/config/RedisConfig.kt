package com.ticketing.seatmanagementservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis와 관련된 설정을 담당하는 클래스
 * RedisTemplate Bean을 직접 생성하여, 데이터가 Redis에 어떻게 저장되고 읽히는지(직렬화/역직렬화)를 명확하게 정의
 */
@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = connectionFactory

        //Key, Value, Hash Key, Hash Value의 Serializer(직렬화기)를 모두 StringRedisSerializer로 설정합니다.
        //이렇게 하면 Redis에 데이터를 저장할 때, 모든 키와 값이 우리가 코드에서 다루는 String 형태로 일관되게 저장됩니다.
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()

        return template
    }
}