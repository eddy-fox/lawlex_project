package com.soldesk.team_project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("member")
public class MemberController {
    
    @GetMapping("/point")
    public String pointMain() {
        return "member/point";
    }
    @PostMapping("/point")
    public String productPayment(@RequestParam("selectedProduct") int product, Model model) {

        model.addAttribute("product", product);

        return "checkout";
    }

}
