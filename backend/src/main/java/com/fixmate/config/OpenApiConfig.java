package com.fixmate.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI fixmateOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FixMate Home Services Booking API")
                        .description("Production REST API documentation for the FixMate platform, enabling customer booking, provider workflows, and administrative management.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("FixMate Engineering Team")
                                .email("support@fixmate.com")
                                .url("https://fixmate.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://fixmate.com/license")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter JWT Bearer token obtained from /api/auth/login")));
    }
}
