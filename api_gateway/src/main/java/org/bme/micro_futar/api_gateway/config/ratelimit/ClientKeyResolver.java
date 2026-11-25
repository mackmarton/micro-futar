package org.bme.micro_futar.api_gateway.config.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.support.ipresolver.RemoteAddressResolver;
import org.springframework.cloud.gateway.support.ipresolver.XForwardedRemoteAddressResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.security.Principal;

/**
 * A rate limit kulcsa: bejelentkezett felhasználónál a munkamenetből vett azonosító, egyébként a kliens IP-címe.
 *
 * <p>Proxy mögött az X-Forwarded-For <em>jobbról</em> számított eleme számít (annyiadik, ahány megbízható proxy
 * van előttünk), mert a bal oldali elemeket a kliens szabadon megadhatja. Proxy nélkül a fejlécet nem vesszük
 * figyelembe. A küldő kilétét ez nem ellenőrzi: a gateway csak a proxyn keresztül lehet elérhető.
 */
@Component("clientKeyResolver")
public class ClientKeyResolver implements KeyResolver {

    private final RemoteAddressResolver addressResolver;

    public ClientKeyResolver(@Value("${app.rate-limit.trusted-proxy-count}") int trustedProxyCount) {
        if (trustedProxyCount < 0) {
            throw new IllegalArgumentException("app.rate-limit.trusted-proxy-count must not be negative");
        }
        this.addressResolver = trustedProxyCount == 0
                ? new RemoteAddressResolver() { }
                : XForwardedRemoteAddressResolver.maxTrustedIndex(trustedProxyCount);
    }

    @Override
    public Mono<String> resolve(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .map(Principal::getName)
                .map(name -> "user:" + name)
                .switchIfEmpty(Mono.fromSupplier(() -> "ip:" + clientAddress(exchange)));
    }

    private String clientAddress(ServerWebExchange exchange) {
        InetSocketAddress address = addressResolver.resolve(exchange);
        // Üres kulcsra a szűrő 403-at adna, ezért ismeretlen cím esetén közös vödör van.
        return address == null ? "unknown" : address.getHostString();
    }
}
