package com.soldesk.team_project.controller;

import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.service.PurchaseService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final PurchaseService purchaseService;
    
    @GetMapping("/point")
    public String pointMain() {
        return "member/point";
    }
    @PostMapping("/point")
    public String productPurchase(@RequestParam("selectedProduct") int productNum, Model model) {

        // ProductDTO product = purchaseService.getProduct(productNum);
        // model.addAttribute("product", product);

        String purchaseId = "order-" + System.currentTimeMillis();
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId); // 회원정보도 같이 줘야함
        model.addAttribute("purchase", purchase);
        
        return "payment/checkout"; // 넘어갈 때 회원 정보 같이 넘겨줘야함
    }

}
