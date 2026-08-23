package es.brasatech.fastbite.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web configuration to serve user-uploaded images.
 */
@Configuration
@ImportRuntimeHints(WebAdapterHints.class)
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantRoutingResolver tenantResolver;

    @Value("${image.upload.directory}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve user-uploaded images from the configured upload directory
        Path uploadPath = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        String uploadLocation = "file:" + uploadPath.toString() + "/";

        registry.addResourceHandler("/user-images/**")
                .addResourceLocations(uploadLocation)
                .setCachePeriod(3600); // Cache for 1 hour

        // System images are already served from /static/images by Spring Boot's default
        // configuration
    }

    @Override
    public void addInterceptors(org.springframework.web.servlet.config.annotation.InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor(tenantResolver))
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico", "/error", "/user-images/**");
    }

    @org.springframework.context.annotation.Bean
    public TenantContextFilter tenantContextFilter() {
        return new TenantContextFilter(tenantResolver);
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<TenantContextFilter> tenantContextFilterRegistration(TenantContextFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<TenantContextFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    @org.springframework.context.annotation.Bean
    @org.springframework.context.annotation.Primary
    public com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}
