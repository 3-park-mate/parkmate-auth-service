package com.parkmate.authservice.authuser.vo.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerifyEmailCodeResponseVo {

    private boolean isValid;

    @Builder
    private VerifyEmailCodeResponseVo(boolean isValid) {
        this.isValid = isValid;
    }

    public static VerifyEmailCodeResponseVo of(boolean isValid) {
        return new VerifyEmailCodeResponseVo(isValid);
    }
}