package com.example.gaokaoproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.gaokaoproject.mapper")
public class GaokaoprojectApplication {

    public static void main(String[] args) {
        SpringApplication.run(GaokaoprojectApplication.class, args);
    }

}
