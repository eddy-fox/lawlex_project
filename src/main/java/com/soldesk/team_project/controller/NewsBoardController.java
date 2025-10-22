package com.soldesk.team_project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.soldesk.team_project.dto.AdminDTO;
import com.soldesk.team_project.dto.CategoryDTO;
import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.service.NewsBoardService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/newsboard")
@RequiredArgsConstructor
public class NewsBoardController {
    
    private final NewsBoardService newsboardService;


    @GetMapping
    public String listBoard(@RequestParam("categoryIdx") int categogyIdx, Model model){

        List<NewsBoardDTO> boardList = newsboardService.getAllBoard(categogyIdx);
        model.addAttribute("boardList", boardList);

        List<CategoryDTO> categoryList = newsboardService.getAllCategory(categogyIdx);
        model.addAttribute("categoryList", categoryList);

        CategoryDTO category = newsboardService.getCategory(categogyIdx);
        model.addAttribute("category", category);

        return "newsboard/list";
    }


    @GetMapping("/write")
    public String formWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard, @RequestParam("categoryIdx") int categogyIdx, @SessionAttribute("loginAdmin") AdminDTO loginAdmin){
        writeBoard.setCategoryIdx(categogyIdx);
        writeBoard.setAdminIdx(loginAdmin.getAdminIdx());
        return "newsboard/write";
    }

    @PostMapping("/write")
    public String submitWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard){
        System.out.println("작성자 정보: " + writeBoard.getLawyerIdx());
        newsboardService.writeProcess(writeBoard);
        return "redirect:/newsboard/list?category_idx=" + writeBoard.getCategoryIdx();
    }

    @GetMapping("/info")
    public String showInfo(@RequestParam("newsIdx") int newsIdx, Model model){
        NewsBoardDTO infoBoard = newsboardService.getNewsBoard(newsIdx);
        model.addAttribute("infoBoard", infoBoard);
        return "newsboard/info";
    }

    @GetMapping("/modify")
    public String formModify(@RequestParam("newsIdx") int newsIdx, Model model){
        NewsBoardDTO modifyBoard = newsboardService.getNewsBoard(newsIdx);
        model.addAttribute("modifyBoard", modifyBoard);
        return "newsboard/modify";
    }

    @PostMapping("/modify")
    public String submitModify(@ModelAttribute("modifyBoard") NewsBoardDTO modifyBoard){
        newsboardService.modifyProcess(modifyBoard);
        return "redirect:/newsboard/info?newsIdx=" + modifyBoard.getNewsIdx();
    }


}
