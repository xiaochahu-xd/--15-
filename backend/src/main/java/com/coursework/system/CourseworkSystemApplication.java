package com.coursework.system;

import com.coursework.system.common.security.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@MapperScan("com.coursework.system.**.mapper")
@EnableConfigurationProperties(JwtProperties.class)
public class CourseworkSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseworkSystemApplication.class, args);
    }
}
