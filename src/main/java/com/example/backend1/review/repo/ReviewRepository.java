package com.example.backend1.review.repo;

import com.example.backend1.company.domain.Company;
import com.example.backend1.review.domain.Review;
import com.example.backend1.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    boolean existsByCompanyAndUser(Company company, User user);

    // 여러 company_id에 대해 한 번에 평균/개수 조회 (findNearby 최적화)
    @Query("SELECT r.company.id, AVG(r.rating), COUNT(r) FROM Review r " +
           "WHERE r.company.id IN :companyIds GROUP BY r.company.id")
    List<Object[]> aggregateByCompanyIds(@Param("companyIds") List<Long> companyIds);
}
