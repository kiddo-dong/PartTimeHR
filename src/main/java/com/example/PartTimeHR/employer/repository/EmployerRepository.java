package com.example.PartTimeHR.employer.repository;

import com.example.PartTimeHR.employer.domain.Employer;
import com.example.PartTimeHR.employer.dto.EmployeeListResponse;
import com.example.PartTimeHR.employer.dto.EmployeeResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// JPA사용 - 빠른개발
public interface EmployerRepository extends JpaRepository<Employer, Long> {

    boolean existsByEmail(String email);

    Optional<Employer> findByEmail(String email);

    // Employer의 주간 시작일 찾기
    @Query("select e.weekStartDay from Employer e where e.id = :employerId")
    Integer findWeekStartDay(Long employerId);

    // 직원 단일 조회
    @Query("""
    select new com.example.PartTimeHR.employer.dto.EmployeeResponse(
        e.id,
        e.email,
        e.name,
        e.phone,
        pp.jobTitle,
        pp.hourlyWage,
        e.createdAt
    )
    from Employee e
    left join e.payPolicy pp
    where e.employer.id = :employerId
      and e.id = :employeeId
""")
    EmployeeResponse findEmployee(@Param("employerId")Long employerId, @Param("employeeId")Long employeeId);

    // 직원 리스트 조회
    @Query("""
    select new com.example.PartTimeHR.employer.dto.EmployeeListResponse(
        e.id,
        e.email,
        e.name,
        e.phone,
        pp.jobTitle,
        pp.hourlyWage,
        e.createdAt
    )
    from Employee e
    left join e.payPolicy pp
    where e.employer.id = :employerId
""")
    List<EmployeeListResponse> findEmployeeList(@Param("employerId") Long employerId);

    // 직급으로 직원 정보 조회 - 조건 전체 리스트
    @Query("""
    select new com.example.PartTimeHR.employer.dto.EmployeeListResponse(
        e.id,
        e.email,
        e.name,
        e.phone,
        pp.jobTitle,
        pp.hourlyWage,
        e.createdAt
    )
    from Employee e
    join e.payPolicy pp
    where e.employer.id = :employerId
    and pp.jobTitle = :jobTitle
""")
    List<EmployeeListResponse> findEmployeeListJobTitle(@Param("employerId") Long employerId, @Param("jobTitle") String jobTitle);
}