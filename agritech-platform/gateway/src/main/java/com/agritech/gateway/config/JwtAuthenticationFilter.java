package com.agritech.gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

public class JwtAuthenticationFilter implements WebFilter {

    private final Function<ServerWebExchange, Boolean> validator;

    public JwtAuthenticationFilter(Function<ServerWebExchange, Boolean> validator) {
        this.validator = validator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        if (!validator.apply(exchange)) {
            return chain.filter(exchange);
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "gateway-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }
}
