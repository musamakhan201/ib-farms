package com.ibfarms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class IbFarmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(IbFarmsApplication.class, args);
    }
}
