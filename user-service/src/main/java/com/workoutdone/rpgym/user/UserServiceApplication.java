package com.workoutdone.rpgym.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// common 모듈(com.workoutdone.rpgym.common)의 GlobalExceptionHandler 등 공통 빈을
// 스캔 범위에 포함시키기 위해 basePackages를 상위 패키지로 넓힌다.
@SpringBootApplication(scanBasePackages = "com.workoutdone.rpgym")
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}