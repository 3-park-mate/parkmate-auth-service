package com.parkmate.authservice.authuser.vo.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailDuplicateResponseVo {

    private boolean isDuplicate;

    @Builder
    private EmailDuplicateResponseVo(boolean isDuplicate) {
        this.isDuplicate = isDuplicate;
    }

    public static EmailDuplicateResponseVo of(boolean isDuplicate) {
        return new EmailDuplicateResponseVo(isDuplicate);
    }
}