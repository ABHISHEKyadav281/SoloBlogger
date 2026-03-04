package com.solo.blogger.repository;

import com.solo.blogger.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByUserId(Long userId, Pageable pageable);
    Long findAllByUserIdAndIsReadFalse(Long userId);
}
