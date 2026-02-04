package es.brasatech.fastbite;

import es.brasatech.fastbite.application.office.I18nConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {"es.brasatech.fastbite"})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @ConfigurationProperties(prefix = "i18n")
    public I18nConfig i18nConfig() {
        return new I18nConfig();
    }

}