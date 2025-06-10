package com.parkmate.authservice.authhost.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BiznoValidationResponseDto {

    @JsonProperty("resultCode")
    private int resultCode;

    @JsonProperty("resultMsg")
    private String resultMsg;

    @JsonProperty("page")
    private int page;

    @JsonProperty("maxpage")
    private int maxpage;

    @JsonProperty("pagecnt")
    private int pagecnt;

    @JsonProperty("totalCount")
    private int totalCount;

    @JsonProperty("items")
    private List<Item> itemList;

    public boolean hasValidBusiness() {
        return itemList != null &&
                itemList.stream()
                        .filter(item -> item != null) // null safe 처리
                        .anyMatch(Item::isActive);
    }

    public List<Item> getValidItemList() {
        if (itemList == null) {
            return List.of();
        }
        return itemList.stream()
                .filter(item -> item != null)
                .toList();
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {

        @JsonProperty("company")
        private String company;

        @JsonProperty("bno")
        private String bno;

        @JsonProperty("cno")
        private String cno;

        @JsonProperty("bsttcd")
        private String statusCode;

        @JsonProperty("bstt")
        private String status;

        @JsonProperty("TaxTypeCd")
        private String taxTypeCd;

        @JsonProperty("taxtype")
        private String taxType;

        @JsonProperty("EndDt")
        private String endDate;

        public boolean isActive() {
            // 무료 API 대응: 상태코드가 없으면 정상으로 간주 (주의: 유료 전환시 다시 엄격 적용 추천)
            return statusCode == null || statusCode.isBlank() || "01".equals(statusCode);
        }
    }
}