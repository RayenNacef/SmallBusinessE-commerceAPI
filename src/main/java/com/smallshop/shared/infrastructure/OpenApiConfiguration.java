package com.smallshop.shared.infrastructure;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Small Shop API",
                version = "v1",
                description = "E-commerce backend for small businesses. Log in via /api/auth/login and use the token with Authorize."),
        security = @SecurityRequirement(name = OpenApiConfiguration.BEARER_SCHEME))
@SecurityScheme(name = OpenApiConfiguration.BEARER_SCHEME, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
class OpenApiConfiguration {

    static final String BEARER_SCHEME = "bearerAuth";
}
