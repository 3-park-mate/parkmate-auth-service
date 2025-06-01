package com.parkmate.authservice.common.exception;

import com.parkmate.authservice.common.response.ApiResponse;
import com.parkmate.authservice.common.response.ResponseStatus;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BaseExceptionHandler {
    /**
     * 발생한 예외 처리
     */
    @ExceptionHandler(BaseException.class)
    protected ApiResponse<Void> BaseError(BaseException e) {
        log.error("BaseException -> {} ({})", e.getStatus(), e.getMessage());
        return ApiResponse.error(e.getStatus());
    }

    @ExceptionHandler(RuntimeException.class)
    protected ApiResponse<Void> RuntimeError(RuntimeException e) {
        log.error("RuntimeException: ", e);
        for (StackTraceElement s : e.getStackTrace()) {
            System.out.println(s);
        }
        return ApiResponse.error(ResponseStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * DTO 유효성 검증 실패 처리 (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("유효성 검사 실패입니다.");
        log.warn("MethodArgumentNotValidException -> {}", message);
        return ApiResponse.of(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * @RequestParam, @PathVariable 유효성 검증 실패 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<String> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("ConstraintViolationException -> {}", ex.getMessage());
        return ApiResponse.of(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * @ModelAttribute 바인딩 오류 처리
     */
    @ExceptionHandler(BindException.class)
    protected ApiResponse<String> handleBindException(BindException ex) {
        String message = ex.getAllErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .findFirst()
                .orElse("잘못된 요청입니다.");
        log.warn("BindException -> {}", message);
        return ApiResponse.of(HttpStatus.BAD_REQUEST, message);
    }
}
