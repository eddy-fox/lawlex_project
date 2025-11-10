
package com.soldesk.team_project.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.service.LawyerService;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final LawyerService lawyerService;

    @GetMapping("/lawyer/pending")
    public String pendingLawyers(Model model) {
        List<LawyerEntity> list = lawyerService.getPending();
        model.addAttribute("list", list);
        return "admin/lawyer-pending";
    }

    @PostMapping("/lawyer/{idx}/approve")
    public String approveLawyer(@PathVariable Integer idx) {
        lawyerService.approve(idx);
        return "redirect:/admin/lawyer/pending";
    }

    @PostMapping("/lawyer/{idx}/reject")
    public String rejectLawyer(@PathVariable Integer idx) {
        lawyerService.reject(idx);
        return "redirect:/admin/lawyer/pending";
    }
}
