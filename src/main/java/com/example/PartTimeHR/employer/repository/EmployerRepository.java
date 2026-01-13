package com.example.PartTimeHR.employer.repository;

import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    boolean existsByEmail(String email);

    Optional<Employer> findByEmail(String email);
}