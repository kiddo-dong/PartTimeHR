package com.example.PartTimeHR.schedule.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employee/schedules")
@PreAuthorize("hasRole('EMPLOYEE')")
public class EmployeeScheduleController {

}
