package com.example.quantserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class QuantServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantServerApplication.class, args);
    }

}
