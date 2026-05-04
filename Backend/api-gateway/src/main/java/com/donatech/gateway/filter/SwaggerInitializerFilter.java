package com.donatech.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class SwaggerInitializerFilter implements GlobalFilter, Ordered {

    private static final Pattern SERVICE_PATTERN =
            Pattern.compile("^/(?!webjars)([^/]+)/swagger-ui/swagger-initializer\\.js$");

    private static final String AGGREGATOR_PATH = "/webjars/swagger-ui/swagger-initializer.js";

    private static final String AGGREGATOR_JS =
            "window.onload = function() {\n" +
            "  window.ui = SwaggerUIBundle({\n" +
            "    urls: [\n" +
            "      { name: \"Auth API\",         url: \"/auth/v3/api-docs\" },\n" +
            "      { name: \"Catalog API\",      url: \"/catalog/v3/api-docs\" },\n" +
            "      { name: \"Users API\",        url: \"/users/v3/api-docs\" },\n" +
            "      { name: \"Order API\",        url: \"/order/v3/api-docs\" },\n" +
            "      { name: \"Supports API\",     url: \"/supports/v3/api-docs\" },\n" +
            "      { name: \"Notification API\", url: \"/notification/v3/api-docs\" },\n" +
            "      { name: \"Shipping API\",     url: \"/shipping/v3/api-docs\" }\n" +
            "    ],\n" +
            "    dom_id: '#swagger-ui',\n" +
            "    deepLinking: true,\n" +
            "    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],\n" +
            "    plugins: [SwaggerUIBundle.plugins.DownloadUrl],\n" +
            "    layout: \"StandaloneLayout\",\n" +
            "    validatorUrl: \"\"\n" +
            "  });\n" +
            "};\n";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (AGGREGATOR_PATH.equals(path)) {
            return writeJs(exchange, AGGREGATOR_JS);
        }

        Matcher matcher = SERVICE_PATTERN.matcher(path);
        if (matcher.matches()) {
            String service = matcher.group(1);
            return writeJs(exchange, buildServiceInitializerJs(service));
        }

        return chain.filter(exchange);
    }

    private Mono<Void> writeJs(ServerWebExchange exchange, String js) {
        byte[] bytes = js.getBytes(StandardCharsets.UTF_8);
        exchange.getResponse().setStatusCode(HttpStatus.OK);
        exchange.getResponse().getHeaders().setContentType(MediaType.parseMediaType("application/javascript"));
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    private String buildServiceInitializerJs(String service) {
        return "window.onload = function() {\n" +
               "  window.ui = SwaggerUIBundle({\n" +
               "    url: \"\",\n" +
               "    dom_id: '#swagger-ui',\n" +
               "    deepLinking: true,\n" +
               "    presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],\n" +
               "    plugins: [SwaggerUIBundle.plugins.DownloadUrl],\n" +
               "    layout: \"StandaloneLayout\",\n" +
               "    \"configUrl\": \"/" + service + "/v3/api-docs/swagger-config\",\n" +
               "    \"validatorUrl\": \"\"\n" +
               "  });\n" +
               "};\n";
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
