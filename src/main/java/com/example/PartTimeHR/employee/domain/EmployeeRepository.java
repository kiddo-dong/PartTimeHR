package com.example.PartTimeHR.employee.domain;

import com.example.PartTimeHR.store.domain.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 응답 변환 시 store/payPolicy를 읽으므로 N+1 방지를 위해 함께 로딩
    @EntityGraph(attributePaths = {"store", "payPolicy"})
    List<Employee> findByStoreId(Long storeId);

    // 특정 직원 + 가게 검증
    @EntityGraph(attributePaths = {"store", "payPolicy"})
    Optional<Employee> findByIdAndStore(Long id, Store store);

    // 시급 정책 삭제 전 사용 중인 직원 존재 확인
    boolean existsByPayPolicyId(Long payPolicyId);

    // 매장 삭제 전 소속 직원 존재 확인
    boolean existsByStoreId(Long storeId);
}
