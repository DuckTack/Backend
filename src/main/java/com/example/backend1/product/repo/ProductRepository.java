package com.example.backend1.product.repo;

import com.example.backend1.product.domain.Product;
import com.example.backend1.diagnosis.domain.IssueType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryAndActiveTrue(IssueType category);
}