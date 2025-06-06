package com.parkmate.authservice.authhost.vo.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostRegisterRequestVo {

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "전화번호는 필수입니다.")
    @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다.")
    private String phoneNumber;

    @NotBlank(message = "계좌번호는 필수입니다.")
    private String accountNumber;

    @NotBlank(message = "사업자등록번호는 필수입니다.")
    private String businessRegistrationNumber;

    @NotNull(message = "정산 주기는 필수입니다.")
    private Integer settlementCycle;
}
