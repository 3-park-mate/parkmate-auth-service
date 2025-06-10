package com.parkmate.authservice.common.mail;

public class MailMessageConstants {

    public static final String ACCOUNT_LOCKED_SUBJECT = "[ParkMate] 계정이 잠금되었습니다";

    public static final String ACCOUNT_LOCKED_BODY_TEMPLATE =
            "안녕하세요, %s님.\n\n" +
                    "로그인 실패가 5회 이상 발생하여 계정이 잠금 처리되었습니다.\n" +
                    "본인이 아닌 경우 즉시 고객센터에 문의 바랍니다.";

    public static final String VERIFICATION_CODE_SUBJECT = "[ParkMate] 이메일 인증 코드 안내";

    public static final String VERIFICATION_CODE_BODY_TEMPLATE =
            "안녕하세요,\n\n" +
                    "요청하신 인증 코드는 아래와 같습니다.\n\n" +
                    "[ 인증 코드: %s ]\n\n" +
                    "해당 코드는 3분 동안만 유효합니다.\n\n" +
                    "감사합니다.";
}
