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
        registry.addInterceptor(new TenantInterceptor());
    }

    @org.springframework.context.annotation.Bean
    public TenantRoutingFilter tenantRoutingFilter() {
        return new TenantRoutingFilter();
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<TenantRoutingFilter> tenantRoutingFilterRegistration(TenantRoutingFilter filter) {
        org.springframework.boot.web.servlet.FilterRegistrationBean<TenantRoutingFilter> registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(org.springframework.core.Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
