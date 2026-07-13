package com.example.PartTimeHR.leave.domain;

import com.example.PartTimeHR.employee.domain.Employee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// 연차 사용 신청 (직원 신청 → 사장 승인/거절)
@Entity
@Table(name = "leave_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    public static LeaveRequest create(Employee employee, LocalDate leaveDate) {
        LeaveRequest request = new LeaveRequest();
        request.employee = employee;
        request.leaveDate = leaveDate;
        request.status = LeaveStatus.PENDING;
        request.createdAt = LocalDateTime.now();
        return request;
    }

    public void approve() {
        requirePending();
        this.status = LeaveStatus.APPROVED;
        this.decidedAt = LocalDateTime.now();
    }

    public void reject() {
        requirePending();
        this.status = LeaveStatus.REJECTED;
        this.decidedAt = LocalDateTime.now();
    }

    private void requirePending() {
        if (status != LeaveStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 연차 신청입니다.");
        }
    }
}
