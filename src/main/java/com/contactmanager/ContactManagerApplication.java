package com.contactmanager;

import com.contactmanager.application.security.AuthCookieProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AuthCookieProperties.class)
public class ContactManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ContactManagerApplication.class, args);
    }
}
