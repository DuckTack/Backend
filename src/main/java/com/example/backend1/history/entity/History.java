package com.example.backend1.history.entity;

import com.example.backend1.common.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class History {

    @Id @GeneratedValue
    private Long id;

    private String username;
    private String issueType;
    private int riskScore;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt = LocalDateTime.now();

    protected History() {}

    public History(String username, String issueType, int riskScore, Status status) {
        this.username = username;
        this.issueType = issueType;
        this.riskScore = riskScore;
        this.status = status;
    }
}
