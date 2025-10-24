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
    private Integer lawyerIdx;
    private String purchaseId;
    private String purchaseState;
    private LocalDate purchaseLegDate;
    
}
