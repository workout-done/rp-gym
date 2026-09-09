package com.workoutdone.rpgym.health.summary.adapter.out;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/internal/users/{userId}/health-contexts")
    UserHealthContextResponse getHealthContext(@PathVariable UUID userId);
}