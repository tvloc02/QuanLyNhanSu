package com.hrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class HrManagementSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrManagementSystemApplication.class, args);
    }
}