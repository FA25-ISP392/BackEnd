package com.isp392.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final String[] POST_PUBLIC_ENDPOINTS = {
            "/auth/**",
            "/customer",
            "/payment/**",
    };

    private final String[] GET_PUBLIC_ENDPOINTS = {
            "/booking/**",
            "/dish/**",
            "/dish-topping/**",
            "/order-details/**",
            "/orders/**",
            "/topping/**",
            "/staff/**",
            "/customer?page=0&size=1000",
            "/statistics/**",
            "/payment/**",
            "/auth/**",
    };

    private final String[] SWAGGER_ENDPOINTS = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        config.addAllowedOriginPattern("http://localhost:8080");
        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedOriginPattern("https://moncuaban.vercel.app");
        config.addAllowedOriginPattern("https://backend-production-0865.up.railway.app");
        config.addAllowedOriginPattern("https://backend2-production-00a1.up.railway.app/");
        config.addAllowedOriginPattern("https://api-monngon88.purintech.id.vn");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");
        config.addExposedHeader("Content-Type");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(request -> request
                        .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.POST, POST_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers(HttpMethod.GET, GET_PUBLIC_ENDPOINTS).permitAll()
                        .requestMatchers("/oauth2/**", "/login/**", "/auth/google/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                        .anyRequest().authenticated()
                ).oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/google")
//                                .defaultSuccessUrl("/auth/google/popup")
                        .defaultSuccessUrl("/auth/google/success", true)
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwtConfigurer ->
                                jwtConfigurer.decoder(jwtDecoder())
                                        .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .csrf(AbstractHttpConfigurer::disable);
        return httpSecurity.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // ✅ BƯỚC 1: BÁO CHO SPRING BIẾT PHẢI ĐỌC QUYỀN TỪ CLAIM "role"
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");

        // ✅ BƯỚC 2: GIỮ NGUYÊN VIỆC THÊM TIỀN TỐ "ROLE_"
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");
        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
