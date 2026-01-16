package com.example.PartTimeHR.employee.repository;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByStoreId(Long storeId);

    // 특정 직원 + 가게 검증
    Optional<Employee> findByIdAndStore(Long id, Store store);
}
