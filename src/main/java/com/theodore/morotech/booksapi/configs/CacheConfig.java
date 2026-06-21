package com.theodore.morotech.booksapi.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.cache.autoconfigure.CacheProperties;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

import java.time.Duration;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfig implements CachingConfigurer {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(10);

    @Bean
    RedisCacheManagerBuilderCustomizer booksCacheCustomizer(CacheProperties cacheProperties,
                                                            @Value("${app.cache.key-prefix}") String keyPrefix) {

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("com.theodore.morotech.booksapi.")
                .allowIfSubType("java.util.")
                .build();

        GenericJacksonJsonRedisSerializer values = GenericJacksonJsonRedisSerializer.builder()
                .enableDefaultTyping(ptv)
                .build();

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(redisCacheTtl(cacheProperties))
                .computePrefixWith(name -> keyPrefix + ":" + name + ":")
                .serializeKeysWith(fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(fromSerializer(values));

        return builder -> builder.cacheDefaults(defaults);
    }

    private static Duration redisCacheTtl(CacheProperties cacheProperties) {
        Duration configuredTtl = cacheProperties.getRedis().getTimeToLive();
        return configuredTtl != null ? configuredTtl : DEFAULT_TTL;
    }

    @Override
    @Bean
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }

}
