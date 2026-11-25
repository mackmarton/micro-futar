package org.bme.micro_futar.api_gateway.config.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.support.ConfigurationService;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A gyári {@link RedisRateLimiter} két kiegészítéssel: elutasításkor Retry-After fejlécet ad, és ha a Redis nem
 * válaszol időben, átengedi a kérést (hibánál a gyári is átenged, de időkorlátja nincs).
 */
public class FailOpenRedisRateLimiter extends RedisRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(FailOpenRedisRateLimiter.class);

    private final Duration timeout;

    public FailOpenRedisRateLimiter(ReactiveStringRedisTemplate redisTemplate,
                                    RedisScript<List<Long>> script,
                                    ConfigurationService configurationService,
                                    Duration timeout) {
        super(redisTemplate, script, configurationService);
        this.timeout = timeout;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        // A Redis-kapcsolat felépítése blokkol: külön szálon fut, hogy az időkorlát érvényesülhessen,
        // és a Netty eseményhurok ne akadjon el.
        return Mono.defer(() -> super.isAllowed(routeId, id))
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(timeout)
                .onErrorResume(error -> {
                    log.warn("Rate limiter unavailable on route {}, request allowed: {}", routeId, error.toString());
                    return Mono.just(new Response(true, Map.of()));
                })
                .map(response -> process(routeId, response));
    }

    private Response process(String routeId, Response response) {
        // A gyári limiter Redis-hiba esetén -1 maradék tokennel enged át.
        if ("-1".equals(response.getHeaders().get(getRemainingHeader()))) {
            log.warn("Redis error in rate limiter on route {}, request allowed", routeId);
        }
        if (response.isAllowed()) {
            return response;
        }

        Config config = getConfig().get(routeId);
        if (config == null) {
            return response;
        }
        // Ennyi másodperc alatt biztosan összegyűlik egy kéréshez elég token.
        long retryAfterSeconds = Math.ceilDiv(config.getRequestedTokens(), config.getReplenishRate());
        Map<String, String> headers = new HashMap<>(response.getHeaders());
        headers.put(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfterSeconds));
        return new Response(false, headers);
    }
}
