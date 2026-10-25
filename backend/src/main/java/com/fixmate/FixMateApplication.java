package com.fixmate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FixMateApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FixMateApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("started");
    }
}
