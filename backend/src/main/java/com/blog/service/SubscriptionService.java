package com.blog.service;

import com.blog.dto.SubscribeRequest;
import com.blog.dto.SubscribeResponse;
import com.blog.entity.Subscriber;
import com.blog.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriberRepository repository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<Subscriber> getSubscribers() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public long getActiveCount() {
        return repository.count(); // simplified: all subscribers are "active" unless manually set
    }

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

        // Send welcome email asynchronously
        emailService.sendWelcomeEmail(email);

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

    @Transactional
    public void deleteSubscriber(Long id) {
        Subscriber subscriber = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("未找到该订阅记录"));
        repository.delete(subscriber);
        log.info("Subscriber deleted: {} ({})", subscriber.getEmail(), id);
    }
}
