package com.tkd.dictionaryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients
@ComponentScan(basePackages = {"com.tkd.security", "com.tkd.dictionaryservice"})
public class DictionaryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DictionaryServiceApplication.class, args);
    }

}
