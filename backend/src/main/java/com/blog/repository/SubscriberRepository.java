package com.blog.repository;

import com.blog.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {

    Optional<Subscriber> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT s.email FROM Subscriber s WHERE s.status = 'active'")
    List<String> findAllActiveEmails();

    List<Subscriber> findAllByOrderByCreatedAtDesc();
}
