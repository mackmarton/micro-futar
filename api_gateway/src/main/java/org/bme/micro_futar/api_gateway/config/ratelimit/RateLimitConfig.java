package org.bme.micro_futar.api_gateway.config.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.time.Duration;
import java.util.List;

@Configuration
public class RateLimitConfig {

    @Bean
    public RedisRateLimiter redisRateLimiter(
            ReactiveStringRedisTemplate redisTemplate,
            @Qualifier("redisRequestRateLimiterScript") RedisScript<List<Long>> script,
            ConfigurationService configurationService,
            @Value("${app.rate-limit.redis-timeout}") Duration timeout) {
        return new FailOpenRedisRateLimiter(redisTemplate, script, configurationService, timeout);
    }
}
