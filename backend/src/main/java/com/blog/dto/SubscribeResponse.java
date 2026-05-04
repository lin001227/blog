package com.blog.dto;

import com.blog.entity.Subscriber;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscribeResponse {

    private Long id;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    public static SubscribeResponse fromEntity(Subscriber subscriber) {
        return new SubscribeResponse(
                subscriber.getId(),
                subscriber.getEmail(),
                subscriber.getStatus(),
                subscriber.getCreatedAt()
        );
    }
}
