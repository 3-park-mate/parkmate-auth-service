package com.parkmate.authservice.common.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class ApiResponse<T> {

    private int code; // ResponseStatus의 커스텀 코드
    private HttpStatus status; // HTTP 상태코드 (예: 400, 200)
    private String message;
    private T data;

    // ✅ 커스텀 ResponseStatus 전용 생성자
    public ApiResponse(ResponseStatus status, T data) {
        this.code = status.getCode();                    // 커스텀 코드
        this.status = status.getHttpStatus();            // HTTP 상태
        this.message = status.getMessage();              // 커스텀 메시지
        this.data = data;
    }

    // ✅ 커스텀 ResponseStatus 전용 응답
    public static <T> ApiResponse<T> of(ResponseStatus status) {
        return new ApiResponse<>(status, null);
    }

    public static <T> ApiResponse<T> of(ResponseStatus status, T data) {
        return new ApiResponse<>(status, data);
    }

    public static <T> ApiResponse<T> error(ResponseStatus status) {
        return new ApiResponse<>(status, null);
    }

    // ✅ 기존 HttpStatus 기반 응답 (성공 응답용으로만 사용하는 것을 권장)
    public ApiResponse(HttpStatus status, String message, T data) {
        this.code = status.value(); // 이건 일반 HttpStatus 코드 사용
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, T data) {
        return of(status, status.name(), data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, HttpStatus.OK.name(), data);
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return of(HttpStatus.OK, message, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return of(HttpStatus.CREATED, data);
    }
}