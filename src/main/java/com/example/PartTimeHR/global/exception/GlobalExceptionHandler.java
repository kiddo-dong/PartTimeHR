package com.example.PartTimeHR.global.exception;

import com.example.PartTimeHR.auth.exception.InvalidCredentialsException;
import com.example.PartTimeHR.employee.exception.EmployeeAccessDeniedException;
import com.example.PartTimeHR.employee.exception.EmployeeEmailDuplicatesException;
import com.example.PartTimeHR.employee.exception.EmployeeNotFoundException;
import com.example.PartTimeHR.employee.exception.PasswordMismatchException;
import com.example.PartTimeHR.employer.exception.EmployerNotFoundException;
import com.example.PartTimeHR.global.dto.ErrorResponse;
import com.example.PartTimeHR.mail.exception.EmailNotVerifiedException;
import com.example.PartTimeHR.schedule.exception.DuplicateScheduleException;
import com.example.PartTimeHR.schedule.exception.InvalidScheduleException;
import com.example.PartTimeHR.schedule.exception.ScheduleAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreAccessDeniedException;
import com.example.PartTimeHR.store.exception.StoreNotFoundException;
import com.example.PartTimeHR.workrecord.exception.WorkRecordNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ===== 401 UNAUTHORIZED ===== */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildError(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage());
    }

    /* ===== 404 NOT_FOUND ===== */
    @ExceptionHandler({
            EmployerNotFoundException.class,
            EmployeeNotFoundException.class,
            StoreNotFoundException.class,
            WorkRecordNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException ex) {
        return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    /* ===== 403 FORBIDDEN ===== */
    @ExceptionHandler({
            StoreAccessDeniedException.class,
            EmployeeAccessDeniedException.class,
            ScheduleAccessDeniedException.class
    })
    public ResponseEntity<ErrorResponse> handleAccessDenied(RuntimeException ex) {
        return buildError(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    // 이메일 인증을 마치지 않은 계정의 로그인
    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleEmailNotVerified(EmailNotVerifiedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", ex.getMessage());
    }

    // @PreAuthorize 실패 (권한 없는 역할의 접근)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSpringAccessDenied(AccessDeniedException ex) {
        return buildError(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다.");
    }

    /* ===== 409 CONFLICT ===== */
    @ExceptionHandler({
            EmployeeEmailDuplicatesException.class,
            DuplicateScheduleException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException ex) {
        return buildError(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    // 이미 진행 중인 근무, 휴게 시작 불가 상태 등
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return buildError(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage());
    }

    /* ===== 400 BAD_REQUEST ===== */
    @ExceptionHandler({
            PasswordMismatchException.class,
            InvalidScheduleException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(RuntimeException ex) {
        return buildError(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage());
    }

    // @Valid 검증 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(", "));

        return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", message);
    }

    /* ===== 500 INTERNAL_SERVER_ERROR ===== */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("처리되지 않은 예외", ex);

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );
    }

    private String formatFieldError(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    private ResponseEntity<ErrorResponse> buildError(
            HttpStatus status, String code, String message) {

        return ResponseEntity
                .status(status)
                .body(ErrorResponse.builder()
                        .code(code)
                        .message(message)
                        .timestamp(LocalDateTime.now())
                        .build());
    }
}
