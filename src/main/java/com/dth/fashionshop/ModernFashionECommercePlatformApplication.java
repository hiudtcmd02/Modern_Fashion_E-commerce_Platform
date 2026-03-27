package com.dth.fashionshop;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class ModernFashionECommercePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModernFashionECommercePlatformApplication.class, args);
    }

}
