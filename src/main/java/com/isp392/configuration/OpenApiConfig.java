package com.isp392.configuration;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class OpenApiConfig {

//  @Bean
//  public OpenAPI customOpenAPI() {
//      return new OpenAPI()
//              .servers(List.of(
////                       new Server().url("https://backend-production-0865.up.railway.app/isp392")
//                      new Server().url("https://api-monngon88.purintech.id.vn/isp392")
//              ));
//  }
}

