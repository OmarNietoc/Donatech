package com.donatech.gateway.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SwaggerUiController {

    private static final String HTML = """
            <!DOCTYPE html>
            <html>
            <head>
              <title>Donatech API</title>
              <meta charset="utf-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css">
            </head>
            <body>
            <div id="swagger-ui"></div>
            <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
            <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-standalone-preset.js"></script>
            <script>
            window.onload = function() {
              window.ui = SwaggerUIBundle({
                urls: [
                  { name: "Auth API",         url: "/auth/v3/api-docs" },
                  { name: "Catalog API",      url: "/catalog/v3/api-docs" },
                  { name: "Users API",        url: "/users/v3/api-docs" },
                  { name: "Order API",        url: "/order/v3/api-docs" },
                  { name: "Supports API",     url: "/supports/v3/api-docs" },
                  { name: "Shipping API",     url: "/shipping/v3/api-docs" }
                ],
                dom_id: '#swagger-ui',
                deepLinking: true,
                presets: [SwaggerUIBundle.presets.apis, SwaggerUIStandalonePreset],
                plugins: [SwaggerUIBundle.plugins.DownloadUrl],
                layout: "StandaloneLayout",
                validatorUrl: ""
              });
            };
            </script>
            </body>
            </html>
            """;

    @GetMapping(value = "/swagger-ui", produces = MediaType.TEXT_HTML_VALUE)
    public String swaggerUi() {
        return HTML;
    }
}
