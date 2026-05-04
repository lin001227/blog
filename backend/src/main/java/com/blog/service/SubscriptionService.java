package com.blog.service;

import com.blog.dto.SubscribeRequest;
import com.blog.dto.SubscribeResponse;
import com.blog.entity.Subscriber;
import com.blog.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriberRepository repository;

    @Transactional
    public SubscribeResponse subscribe(SubscribeRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // Check if already subscribed
        if (repository.existsByEmail(email)) {
            throw new RuntimeException("该邮箱已订阅 ✓");
        }

        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);
        subscriber.setStatus("active");

        Subscriber saved = repository.save(subscriber);
        log.info("New subscriber: {}", email);
        return SubscribeResponse.fromEntity(saved);
    }

    @Transactional
    public void unsubscribe(String email) {
        Subscriber subscriber = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("未找到该邮箱的订阅记录"));
        subscriber.setStatus("inactive");
        repository.save(subscriber);
        log.info("Unsubscribed: {}", email);
    }
}
