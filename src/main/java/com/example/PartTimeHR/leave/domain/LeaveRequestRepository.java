package com.example.PartTimeHR.leave.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findAllByEmployeeOrderByLeaveDateDesc(Employee employee);

    // 사장의 매장별 신청 목록
    @EntityGraph(attributePaths = "employee")
    List<LeaveRequest> findAllByEmployee_Store_IdOrderByLeaveDateDesc(Long storeId);

    @EntityGraph(attributePaths = "employee")
    List<LeaveRequest> findAllByEmployee_Store_IdAndStatusOrderByLeaveDateDesc(Long storeId, LeaveStatus status);

    // 같은 날 중복 신청 방지
    boolean existsByEmployeeAndLeaveDateAndStatusIn(
            Employee employee, LocalDate leaveDate, Collection<LeaveStatus> statuses);

    // 급여 계산용: 기간 내 승인된 연차
    List<LeaveRequest> findAllByEmployeeAndStatusAndLeaveDateBetween(
            Employee employee, LeaveStatus status, LocalDate from, LocalDate to);

    // 매장 급여 요약·근태 통계용
    @EntityGraph(attributePaths = "employee")
    List<LeaveRequest> findAllByEmployee_Store_IdAndStatusAndLeaveDateBetween(
            Long storeId, LeaveStatus status, LocalDate from, LocalDate to);

    // 잔여 계산용: 기간 내 승인 사용 수
    long countByEmployeeAndStatusAndLeaveDateBetween(
            Employee employee, LeaveStatus status, LocalDate from, LocalDate to);

    long countByEmployeeAndStatus(Employee employee, LeaveStatus status);

    Optional<LeaveRequest> findByIdAndEmployee_Store_Id(Long id, Long storeId);
}
