package com.soldesk.team_project.controller;

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
    public String productPayment(@RequestParam("selectedProduct") int productNum, Model model) {

        ProductDTO product = purchaseService.getProduct(productNum);
        model.addAttribute("product", product);
        // PurchaseDTO purchase = purchaseService.purchaseProduct(product); // 구매하는 멤버 정보 추가해야함
        
        return "checkout";
    }

}
