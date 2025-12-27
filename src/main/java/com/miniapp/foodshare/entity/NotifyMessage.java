package com.miniapp.foodshare.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Notify_Message")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotifyMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "template_id", nullable = false)
    private int templateId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}