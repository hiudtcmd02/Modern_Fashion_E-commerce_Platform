package com.dth.fashionshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class ModernFashionECommercePlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModernFashionECommercePlatformApplication.class, args);
    }

}
