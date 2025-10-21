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
    public String listBoard(@RequestParam("category_idx") int categogy_idx, Model model){

        List<NewsBoardDTO> boardList = newsboardService.getAllBoard(categogy_idx);
        model.addAttribute("boardList", boardList);

        List<CategoryDTO> categoryList = newsboardService.getAllCategory(categogy_idx);
        model.addAttribute("categoryList", categoryList);

        CategoryDTO category = newsboardService.getCategory(categogy_idx);
        model.addAttribute("category", category);

        return "newsboard/list";
    }


    @GetMapping("/write")
    public String formWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard, @RequestParam("category_idx") int categogy_idx, @SessionAttribute("loginAdmin") AdminDTO loginAdmin){
        writeBoard.setCategory_idx(categogy_idx);
        writeBoard.setAdmin_idx(loginAdmin.getAdmin_idx());
        return "newsboard/write";
    }

    @PostMapping("/write")
    public String submitWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard){
        System.out.println("작성자 정보: " + writeBoard.getLawyer_idx());
        newsboardService.writeProcess(writeBoard);
        return "redirect:/newsboard/list?category_idx=" + writeBoard.getCategory_idx();
    }


}
