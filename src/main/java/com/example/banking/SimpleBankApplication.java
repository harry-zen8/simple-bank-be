package com.example.banking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration.class})
public class SimpleBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleBankApplication.class, args);
    }

} 