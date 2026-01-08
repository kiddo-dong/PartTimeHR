package com.example.PartTimeHR.employer.repository;

import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

// JPA사용 - 빠른개발
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    boolean existsByEmail(String email);

    Optional<Employer> findByEmail(String email);

    @Query("select e.weekStartDay from Employer e where e.id = :employerId")
    Integer findWeekStartDay(Long employerId);
}