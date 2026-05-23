package com.example.quantserver.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    // 인증 없이 호출 가능한 공개 API 경로
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/refresh"
    );

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList("bearerAuth");

        return new OpenAPI()
                .info(new Info()
                        .title("Quant Server API")
                        .description("퀀트 서버 API 문서")
                        .version("v1.0"))
                .addSecurityItem(securityRequirement)
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", securityScheme));
    }

    @Bean
    public OpenApiCustomizer publicPathSecurityCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> {
            if (PUBLIC_PATHS.contains(path)) {
                pathItem.readOperations()
                        .forEach(op -> op.setSecurity(Collections.emptyList()));
            }
        });
    }
}
