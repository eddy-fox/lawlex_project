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
    public String pointPayment(
        @RequestParam("pointOption") String pointOption,
        @RequestParam("agree") String agree, Model model
    ) throws Exception {

        String[] parts = pointOption.split(":");
        String amount = parts[1];
        String point = parts[0];

        model.addAttribute("amount", amount);
        model.addAttribute("point", point);

        return "payment/checkout";
    }

}
