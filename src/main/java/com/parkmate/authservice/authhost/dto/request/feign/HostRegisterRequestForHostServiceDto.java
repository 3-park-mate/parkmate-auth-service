package com.parkmate.authservice.authhost.dto.request.feign;

import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostRegisterRequestForHostServiceDto {

    private String hostUuid;
    private String name;
    private String phoneNumber;
    private String bankName;
    private String accountNumber;
    private String businessRegistrationNumber;
    @Schema(description = "정산 주기 (15일 또는 30일)", example = "15")
    private int settlementCycle;

    @Builder
    private HostRegisterRequestForHostServiceDto(String hostUuid,
                                                 String name,
                                                 String phoneNumber,
                                                 String bankName,
                                                 String accountNumber,
                                                 String businessRegistrationNumber,
                                                 int settlementCycle) {

        this.hostUuid = hostUuid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.settlementCycle = settlementCycle;
    }

    public static HostRegisterRequestForHostServiceDto of(String hostUuid, HostRegisterRequestVo hostRegisterRequestVo) {
        return HostRegisterRequestForHostServiceDto.builder()
                .hostUuid(hostUuid)
                .name(hostRegisterRequestVo.getName())
                .phoneNumber(hostRegisterRequestVo.getPhoneNumber())
                .bankName(hostRegisterRequestVo.getBankName())
                .accountNumber(hostRegisterRequestVo.getAccountNumber())
                .businessRegistrationNumber(hostRegisterRequestVo.getBusinessRegistrationNumber())
                .settlementCycle(hostRegisterRequestVo.getSettlementCycle())
                .build();
    }
}