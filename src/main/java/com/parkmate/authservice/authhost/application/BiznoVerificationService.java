package com.parkmate.authservice.authhost.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkmate.authservice.authhost.dto.response.BiznoValidationResponseDto;
import com.parkmate.authservice.authhost.infrastructure.client.BiznoFeignClient;
import com.parkmate.authservice.common.exception.BaseException;
import com.parkmate.authservice.common.response.ResponseStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class BiznoVerificationService {

    private final BiznoFeignClient biznoFeignClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${external.bizno.api-key}")
    private String biznoApiKey;

    private static final Pattern BIZNO_PATTERN = Pattern.compile("\\d{10}");

    public void verify(String bizNumber) {
        String normalizedBizNo = bizNumber.replaceAll("-", "").trim();
        validateBizNoFormat(normalizedBizNo);

        log.info("사업자등록번호 검증 요청 - 번호: {}", normalizedBizNo);

        try {
            BiznoValidationResponseDto response = biznoFeignClient.validateBizNumber(
                    biznoApiKey,
                    "1",
                    normalizedBizNo,
                    "json"
            );

            log.info("✅ Bizno API 원본 응답: {}", objectMapper.writeValueAsString(response));

            if (response == null) {
                throw new BaseException(ResponseStatus.AUTH_BIZNO_API_FAILED, "응답 없음");
            }

            if (!response.hasValidBusiness()) {
                String reason;
                if (response.getValidItemList().isEmpty()) {
                    reason = "응답 항목 없음";
                } else {
                    BiznoValidationResponseDto.Item item = response.getValidItemList().get(0);
                    reason = String.format("사업자 상태코드: %s, 상태명: %s", item.getStatusCode(), item.getStatus());
                }

                log.warn("⚠️ 유효하지 않은 사업자번호(완화 정책 확인 필요) - 번호: {}, 사유: {}", normalizedBizNo, reason);
                throw new BaseException(ResponseStatus.AUTH_BUSINESS_NUMBER_INVALID, reason);
            }

            log.info("✅ 사업자등록번호 검증 성공 - 번호: {}", normalizedBizNo);

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "API 호출 실패";
            log.error("사업자번호 검증 실패 - 번호: {}, 오류: {}", normalizedBizNo, msg, e);
            throw new BaseException(ResponseStatus.AUTH_BIZNO_API_FAILED, msg);
        }
    }

    private void validateBizNoFormat(String bizNo) {
        if (!BIZNO_PATTERN.matcher(bizNo).matches()) {
            throw new BaseException(ResponseStatus.AUTH_BUSINESS_NUMBER_INVALID, "사업자등록번호는 10자리 숫자여야 합니다.");
        }
    }
}