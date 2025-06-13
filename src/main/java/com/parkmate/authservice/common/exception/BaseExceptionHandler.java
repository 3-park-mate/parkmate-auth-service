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
}