package com.example.backend1.company.repo;

import com.example.backend1.company.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

  Page<Company> findByActive(boolean active, Pageable pageable);
  List<Company> findByActiveTrueAndServiceRegionLabelContaining(String region);
}
