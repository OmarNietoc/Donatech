package com.donatech.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${donatech.app.jwtSecret}")
    private String jwtSecret;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // Endpoints internos (comunicación entre microservicios) nunca se exponen vía gateway
            if (exchange.getRequest().getURI().getPath().contains("/internal/")) {
                return onError(exchange, "Forbidden", HttpStatus.FORBIDDEN);
            }
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsHeader(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange, "Missing authorization header", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7);
                }

                try {
                    Claims claims = validateToken(authHeader);
                    List<String> roles = claims.get("roles", List.class);
                    String email = claims.getSubject();
                    Object userIdClaim = claims.get("userId");
                    String userId = userIdClaim != null ? String.valueOf(userIdClaim) : email;

                    ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Email", email)
                            .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                            .build();

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());

                } catch (Exception e) {
                    return onError(exchange, "Invalid access token", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }

    private Claims validateToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
    }

    @Component
    public static class RouteValidator {
        // Abiertos para cualquier método (login/registro son POST, swagger/eureka infraestructura)
        public static final List<String> openAllMethods = List.of(
                "/api/auth/register",
                "/api/auth/login",
                "/swagger-ui",
                "/v3/api-docs",
                "/eureka");

        // Contenido público: solo lectura. Escritura exige JWT.
        public static final List<String> openGetOnly = List.of(
                "/api/products",
                "/api/categories",
                "/api/units",
                "/api/campaigns",
                "/api/kits",
                "/api/regions",
                "/api/comunas",
                "/api/config");

        public java.util.function.Predicate<ServerHttpRequest> isSecured = request -> {
            String path = request.getURI().getPath();
            boolean openForAll = openAllMethods.stream().anyMatch(path::startsWith);
            boolean openForGet = org.springframework.http.HttpMethod.GET.equals(request.getMethod())
                    && openGetOnly.stream().anyMatch(path::startsWith);
            return !(openForAll || openForGet);
        };
    }

    private final RouteValidator validator = new RouteValidator();
}
