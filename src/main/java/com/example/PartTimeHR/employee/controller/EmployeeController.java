package com.example.PartTimeHR.employee.controller;

import com.example.PartTimeHR.employee.dto.CreateEmployeeRequest;
import com.example.PartTimeHR.employee.dto.EmployeeInfoResponse;
import com.example.PartTimeHR.employee.service.EmployeeService;
import com.example.PartTimeHR.security.customuser.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    // 사장님만 접근 가능
    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployeeInfoResponse> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long employerId = userDetails.getId();

        EmployeeInfoResponse response =
                employeeService.createEmployee(request, employerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

