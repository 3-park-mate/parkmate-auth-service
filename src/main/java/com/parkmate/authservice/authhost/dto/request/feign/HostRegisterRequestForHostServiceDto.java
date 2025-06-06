package com.parkmate.authservice.authhost.dto.request.feign;

import com.parkmate.authservice.authhost.vo.request.HostRegisterRequestVo;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class HostRegisterRequestForHostServiceDto {

    private String hostUuid;
    private String name;
    private String phoneNumber;
    private String accountNumber;
    private String businessRegistrationNumber;
    private int settlementCycle;

    @Builder
    private HostRegisterRequestForHostServiceDto(String hostUuid,
                                                 String name,
                                                 String phoneNumber,
                                                 String accountNumber,
                                                 String businessRegistrationNumber,
                                                 int settlementCycle) {

        this.hostUuid = hostUuid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.accountNumber = accountNumber;
        this.businessRegistrationNumber = businessRegistrationNumber;
        this.settlementCycle = settlementCycle;
    }

    public static HostRegisterRequestForHostServiceDto of(String hostUuid, HostRegisterRequestVo hostRegisterRequestVo) {

        return HostRegisterRequestForHostServiceDto.builder()
                .hostUuid(hostUuid)
                .name(hostRegisterRequestVo.getName())
                .phoneNumber(hostRegisterRequestVo.getPhoneNumber())
                .accountNumber(hostRegisterRequestVo.getAccountNumber())
                .businessRegistrationNumber(hostRegisterRequestVo.getBusinessRegistrationNumber())
                .settlementCycle(hostRegisterRequestVo.getSettlementCycle())
                .build();
    }
}
