package com.parkmate.authservice.authhost.vo.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostRegisterRequestVo {

    @NotBlank(message = "이메일은 필수 입력값입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Size(min = 2, max = 50, message = "이름은 2~50자 이내여야 합니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함한 8~20자여야 합니다."
    )
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^010\\d{8}$",
            message = "전화번호는 010으로 시작하는 11자리 숫자여야 합니다."
    )
    private String phoneNumber;

    @NotBlank(message = "계좌번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^[0-9-]{9,30}$",
            message = "계좌번호는 숫자와 '-'만 포함할 수 있으며 9~30자여야 합니다."
    )
    private String accountNumber;

    @NotBlank(message = "사업자등록번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^\\d{10}$",
            message = "사업자등록번호는 숫자 10자리여야 합니다."
    )
    private String businessRegistrationNumber;

    // 정산주기는 컨트롤러 단에서 추가 로직으로 검증 권장 (ex: 1, 7, 30일 단위 등)
    private int settlementCycle;
}
