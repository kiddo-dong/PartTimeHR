package com.example.PartTimeHR.employee.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import com.example.PartTimeHR.schedule.domain.Schedule;
import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    List<Employee> findByStoreId(Long storeId);

    // 특정 직원 + 가게 검증
    Optional<Employee> findByIdAndStore(Long id, Store store);

    // 매장 소속 직원 이메일 중복 확인
    boolean existsByStore_IdAndEmail(Long storeId, String email);
}