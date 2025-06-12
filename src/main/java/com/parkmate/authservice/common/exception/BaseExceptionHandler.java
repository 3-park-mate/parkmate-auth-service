package com.parkmate.authservice.common.exception;

import com.parkmate.authservice.common.response.ApiResponse;
import com.parkmate.authservice.common.response.ResponseStatus;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {

    @ExceptionHandler(BaseException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBaseError(BaseException e) {

        log.error("BaseException -> {} ({})", e.getStatus(), e.getMessage());
        return ResponseEntity
                .status(e.getStatus().getHttpStatus())
                .body(ApiResponse.error(e.getStatus()));
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<ApiResponse<Void>> handleRuntimeError(RuntimeException e) {

        log.error("RuntimeException: ", e);
        for (StackTraceElement s : e.getStackTrace()) {
            System.out.println(s);
        }
        return ResponseEntity
                .status(ResponseStatus.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ResponseStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiResponse<String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검사 실패입니다.");
        log.warn("MethodArgumentNotValidException -> {}", message);

        return ResponseEntity
                .status(ResponseStatus.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseStatus.INVALID_REQUEST.getCode(), message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {

        log.warn("ConstraintViolationException -> {}", ex.getMessage());
        return ResponseEntity
                .status(ResponseStatus.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseStatus.INVALID_REQUEST.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<ApiResponse<String>> handleBindException(BindException ex) {

        String message = ex.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("잘못된 요청입니다.");
        log.warn("BindException -> {}", message);

        return ResponseEntity
                .status(ResponseStatus.INVALID_REQUEST.getHttpStatus())
                .body(ApiResponse.error(ResponseStatus.INVALID_REQUEST.getCode(), message));
    }
}