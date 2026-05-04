package com.blog.controller;

import com.blog.dto.SubscribeRequest;
import com.blog.dto.SubscribeResponse;
import com.blog.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService service;

    @PostMapping("/api/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscribeRequest request) {
        try {
            SubscribeResponse response = service.subscribe(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", "🎉 订阅成功！感谢你的关注", "data", response));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
