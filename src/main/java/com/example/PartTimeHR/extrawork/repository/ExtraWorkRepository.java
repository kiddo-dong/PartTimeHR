package com.example.PartTimeHR.extrawork.repository;

import com.example.PartTimeHR.extrawork.domain.ExtraWork;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExtraWorkRepository extends JpaRepository<ExtraWork, Long> {

    // 특정 직원의 특정 날짜 추가 근무
    List<ExtraWork> findByEmployeeIdAndWorkDate(
            Long employeeId,
            LocalDate workDate
    );

    // 가게의 특정 날짜 추가 근무
    List<ExtraWork> findByStoreIdAndWorkDate(
            Long storeId,
            LocalDate workDate
    );
}
