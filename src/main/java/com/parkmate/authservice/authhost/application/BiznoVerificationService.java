package com.parkmate.authservice.authhost.application;

import com.parkmate.authservice.authhost.dto.response.BiznoValidationResponseDto;
import com.parkmate.authservice.authhost.infrastructure.client.BiznoFeignClient;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiznoVerificationService {

    private final BiznoFeignClient biznoFeignClient;

    @Value("${external.bizno.api-key}")
    private String biznoApiKey;

    public void verify(String bizNumber) {
        String normalizedBizNo = bizNumber.replaceAll("-", "").trim();
        log.info("사업자등록번호 검증 요청 - 번호: {}", normalizedBizNo);

        try {
            BiznoValidationResponseDto response = biznoFeignClient.validateBizNumber(
                    biznoApiKey,
                    "1",                 // gb: 1 → 사업자등록번호로 검색
                    normalizedBizNo,     // 하이픈 제거된 번호 그대로 사용
                    "xml"
            );

            if (response == null || !response.isValid()) {
                String msg = response != null ? response.getMessage() : "응답 없음";
                log.error("유효하지 않은 사업자번호 - 번호: {}, 사유: {}", normalizedBizNo, msg);
                throw new BaseException(ResponseStatus.AUTH_BIZNO_INVALID, msg);
            }

            log.info("사업자등록번호 검증 성공 - 번호: {}", normalizedBizNo);

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "API 호출 실패";
            log.error("사업자번호 검증 실패 - 번호: {}, 오류: {}", normalizedBizNo, msg, e);
            throw new BaseException(ResponseStatus.AUTH_BIZNO_API_FAILED, msg);
        }
    }
}