package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.service.PurchaseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final PurchaseService purchaseService;
    
    // 멤버 포인트
    @GetMapping("/point")
    public String pointMain(Model model) {
        
        int memberIdx = 1; // 회원정보 받아와서 넘겨야함
        
        // 포인트 구매 상품
        List<ProductDTO> productList = purchaseService.getBuyPointProduct();
        model.addAttribute("productList", productList);
        
        // 포인트 사용 내역 및 결제 내역
        List<PointDTO> pointList = purchaseService.getAllPoint(memberIdx);
        List<PurchaseDTO> purchaseList = purchaseService.getAllPurchase(memberIdx);

        model.addAttribute("pointList", pointList);
        model.addAttribute("purchaseList", purchaseList);
        
        return "member/point";
    }
    @PostMapping("/point")
    public String productPurchase(@RequestParam("selectedProduct") int productNum, Model model) {

        // 구매 검증을 위한 ID 생성
        String purchaseId = "order-" + System.currentTimeMillis();
        // 구매 요청 내역 생성
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId); // 회원정보도 같이 줘야함
        model.addAttribute("purchase", purchase);
        
        return "payment/checkout"; // 넘어갈 때 회원 정보 같이 넘겨줘야함
    }

}
