package com.example.PartTimeHR.employer.repository;

import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {

}