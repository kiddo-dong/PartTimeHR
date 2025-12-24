package com.example.PartTimeHR.employer.repository;

import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JPA사용 - 빠른개발
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    boolean existsByEmail(String email);

    Optional<Employer> findByEmail(String email);
}