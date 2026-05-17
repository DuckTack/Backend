package com.example.backend1.review.domain;

import com.example.backend1.company.domain.Company;
import com.example.backend1.user.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "reviews",
        indexes = { @Index(name = "idx_reviews_company_id", columnList = "company_id") },
        uniqueConstraints = { @UniqueConstraint(name = "uq_reviews_company_user", columnNames = {"company_id", "user_id"}) })
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int rating; // 1~5

    @Column(length = 1000)
    private String content;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected Review() {}

    public Review(Company company, User user, int rating, String content) {
        this.company = company;
        this.user = user;
        this.rating = rating;
        this.content = content;
    }

    public Long getId() { return id; }
    public Company getCompany() { return company; }
    public User getUser() { return user; }
    public int getRating() { return rating; }
    public String getContent() { return content; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
