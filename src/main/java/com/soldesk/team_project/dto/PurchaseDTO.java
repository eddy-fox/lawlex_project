package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseDTO {

    private Integer purchaseIdx;
    private Integer productIdx;
    private Integer memberIdx;
    private String purchaseId;
    private String purchaseState;
    private LocalDate purchaseLegDate;

    // 추가 메타 정보 (템플릿 표현용)
    private String productPrice;
    private String productContent;
    
}
