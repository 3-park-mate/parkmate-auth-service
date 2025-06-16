package com.parkmate.authservice.common.exception;

import com.parkmate.authservice.common.response.ApiResponse;
import com.parkmate.authservice.common.response.ResponseStatus;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.FieldError;
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
     * @Valid 기반 바디 유효성 검증 실패 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ApiResponse<Void> handleValidation(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();

        if (error == null) {
            return ApiResponse.of(ResponseStatus.INVALID_REQUEST);
        }

        String field = error.getField();
        String message = error.getDefaultMessage();

        log.warn("Validation failed - field: {}, message: {}", field, message);

        return switch (field) {
            case "email" -> ApiResponse.of(ResponseStatus.INVALID_EMAIL_FORMAT);
            case "name" -> ApiResponse.of(ResponseStatus.INVALID_NAME_FORMAT);
            case "password" -> ApiResponse.of(ResponseStatus.INVALID_PASSWORD_FORMAT);
            case "phoneNumber" -> ApiResponse.of(ResponseStatus.INVALID_PHONE_NUMBER_FORMAT);
            case "verificationCode" -> ApiResponse.of(ResponseStatus.INVALID_VERIFICATION_CODE);

            case "accountNumber" -> ApiResponse.of(ResponseStatus.INVALID_ACCOUNT_NUMBER_FORMAT);
            case "businessRegistrationNumber" -> ApiResponse.of(ResponseStatus.INVALID_BUSINESS_NUMBER_FORMAT);
            case "settlementCycle" -> ApiResponse.of(ResponseStatus.INVALID_SETTLEMENT_CYCLE);


            default -> ApiResponse.of(ResponseStatus.INVALID_REQUEST);
        };
    }

    /**
     * @Validated 기반 파라미터 유효성 검증 실패 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    protected ApiResponse<Void> handleConstraint(ConstraintViolationException e) {
        String message = e.getConstraintViolations()
                .stream()
                .map(v -> v.getMessage())
                .findFirst()
                .orElse(ResponseStatus.INVALID_REQUEST.getMessage());

        log.warn("Constraint violation: {}", message);
        return ApiResponse.of(ResponseStatus.INVALID_REQUEST.getHttpStatus(), message, null);
    }
}