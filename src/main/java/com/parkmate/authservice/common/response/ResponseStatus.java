package com.parkmate.authservice.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseStatus {
    /**
     * 2xx: 요청 성공
     */
    SUCCESS(HttpStatus.OK, true, 200, "요청에 성공하였습니다."),
    CREATED(HttpStatus.CREATED, true, 201, "회원가입 되었습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, true, 205, "로그아웃 되었습니다."),

    /**
     * 4xx: 클라이언트 오류
     */
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "리소스가 존재하지 않습니다."),
    REQUEST_CONFLICT(HttpStatus.CONFLICT, false, 409, "POST 요청에 실패했습니다."),
    INVALID_AUTH_PASSWORD(HttpStatus.UNAUTHORIZED, false, 40101, "비밀번호가 일치하지 않습니다."),
    AUTH_USER_NOT_FOUND(HttpStatus.NOT_FOUND, false, 40401, "존재하지 않는 사용자입니다."),
    AUTH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, false, 40102, "토큰이 만료되었습니다."),
    AUTH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, false, 40103, "유효하지 않은 토큰입니다."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, false, 40301, "접근 권한이 없습니다."),

    AUTH_EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, false, 40901, "이미 존재하는 이메일입니다."),
    AUTH_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, false, 40104, "이메일 인증에 실패하였습니다."),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, false, 40001, "비밀번호 형식이 올바르지 않습니다."),


    AUTH_ACCOUNT_LOCKED(HttpStatus.LOCKED, false, 42301, "로그인 실패가 누적되어 계정이 잠금 처리되었습니다. 이메일을 확인하세요."),
    AUTH_LOGIN_FAILED_TOO_MANY_TIMES(HttpStatus.UNAUTHORIZED, false, 40104, "로그인 시도 횟수가 초과되었습니다."),
    AUTH_EMAIL_SENT_AFTER_LOCK(HttpStatus.OK, true, 20001, "계정이 잠금 처리되었으며, 이메일 알림을 전송했습니다."),

    /**
     * 5xx: 서버 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버 내부 오류가 발생했습니다.");


    private final HttpStatus httpStatus;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
