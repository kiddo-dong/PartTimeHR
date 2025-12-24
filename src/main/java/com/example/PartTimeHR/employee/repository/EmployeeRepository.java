package com.example.PartTimeHR.employee.repository;

import com.example.PartTimeHR.employee.Employee;
import com.example.PartTimeHR.employer.domain.Employer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    List<Employee> findByEmployer(Employer employer);
}

