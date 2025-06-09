package com.parkmate.authservice.authuser.vo.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class EmailDuplicateCheckRequestVo {

    private String email;
}