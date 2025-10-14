//package com.isp392.configuration;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins(
//                        "https://food-system-demo.vercel.app",
//                        "http://localhost:5173"
//                )
//                // Cho phép tất cả method cần thiết
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                // Cho phép mọi header (Authorization, Content-Type, v.v.)
//                .allowedHeaders("*")
//                // Cho phép FE đọc header JWT/token trong response
//                .exposedHeaders("Authorization", "Content-Type")
//                // Cho phép FE gửi kèm cookie/token
//                .allowCredentials(true)
//                // Cache preflight request 1 tiếng
//                .maxAge(3600);
//    }
//}
