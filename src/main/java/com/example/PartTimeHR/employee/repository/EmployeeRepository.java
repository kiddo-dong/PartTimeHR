package com.example.PartTimeHR.employee.repository;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.EmployeeListResponse;
import com.example.PartTimeHR.employer.dto.EmployerInfoResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    Optional<Employee> findByEmail(String email);

    boolean existsByIdAndEmployerId(Long employeeId, Long employerId);

}