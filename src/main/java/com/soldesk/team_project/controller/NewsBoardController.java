package com.soldesk.team_project.controller;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.soldesk.team_project.dto.AdminDTO;
import com.soldesk.team_project.dto.CategoryDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.service.NewsBoardService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/newsboard")
@RequiredArgsConstructor
public class NewsBoardController {
    
    private final NewsBoardService newsboardService;

    private static final Set<Integer> ADMIN_CATEGORIES = Set.of(1, 2, 3);
    private static final int LAWYER_CATEGORY = 4;

    private boolean isAdminCategory(int cat) {
        return ADMIN_CATEGORIES.contains(cat);
    }

    private boolean isLawyerCategory(int cat) {
        return cat == LAWYER_CATEGORY;
    }


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
    public String formWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard, @RequestParam("categoryIdx") int categoryIdx, 
                            @SessionAttribute(value = "loginAdmin", required = false) AdminDTO loginAdmin,
                            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                            HttpServletResponse response) throws IOException{
        writeBoard.setCategoryIdx(categoryIdx);
         if (isAdminCategory(categoryIdx)) {
            if (loginAdmin == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자만 작성할 수 있는 게시판입니다.");
                return null;
            }
            writeBoard.setAdminIdx(loginAdmin.getAdminIdx());
            writeBoard.setLawyerIdx(null); // 안전하게 비움
        } else if (isLawyerCategory(categoryIdx)) {
            if (loginLawyer == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "회원(변호사)만 작성할 수 있는 게시판입니다.");
                return null;
            }
            writeBoard.setLawyerIdx(loginLawyer.getLawyerIdx());
            writeBoard.setAdminIdx(null); // 안전하게 비움
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "유효하지 않은 카테고리입니다.");
            return null;
        }
        return "newsboard/write";
    }

    @PostMapping("/write")
    public String submitWrite(@ModelAttribute("writeBoard") NewsBoardDTO writeBoard) throws Exception{
        if (writeBoard.getAdminIdx() == null){
            System.out.println("작성자 정보: " + writeBoard.getLawyerIdx());
        }else{
            System.out.println("작성자 정보: " + writeBoard.getAdminIdx());
        }
        newsboardService.writeProcess(writeBoard);
        return "redirect:/newsboard/list?category_idx=" + writeBoard.getCategoryIdx();
    }

    @GetMapping("/info")
    public String showInfo(@RequestParam("newsIdx") int newsIdx, Model model){
        NewsBoardDTO infoBoard = newsboardService.getNewsBoard(newsIdx);
        model.addAttribute("infoBoard", infoBoard);
        return "newsboard/info";
    }

    private boolean isOwner(NewsBoardDTO origin,
                            AdminDTO loginAdmin,
                            LawyerDTO loginLawyer) {
        if (isAdminCategory(origin.getCategoryIdx())) {
            // 카테고리 1~3: 관리자만 작성 → 관리자 소유자 일치 검사
            return loginAdmin != null
                    && origin.getAdminIdx() != null
                    && origin.getAdminIdx().equals(loginAdmin.getAdminIdx());
        } else if (isLawyerCategory(origin.getCategoryIdx())) {
            // 카테고리 4: 변호사(회원)만 작성 → 변호사 소유자 일치 검사
            return loginLawyer != null
                    && origin.getLawyerIdx() != null
                    && origin.getLawyerIdx().equals(loginLawyer.getLawyerIdx());
        }
        return false;
    }

    @GetMapping("/modify")
    public String formModify(@RequestParam("newsIdx") int newsIdx,
                            @SessionAttribute(value = "loginAdmin", required = false) AdminDTO loginAdmin,
                            @SessionAttribute(value = "loginLawyer", required = false) LawyerDTO loginLawyer,
                            Model model,
                            RedirectAttributes ra){
        NewsBoardDTO modifyBoard = newsboardService.getNewsBoard(newsIdx);

        if (!isOwner(modifyBoard, loginAdmin, loginLawyer)) {
        ra.addFlashAttribute("error", "작성자만 수정할 수 있습니다.");
        return "redirect:/newsboard/info?newsIdx=" + newsIdx;  // ← 여기!
    }

        model.addAttribute("modifyBoard", modifyBoard);
        return "newsboard/modify";
    }

    @PostMapping("/modify")
    public String submitModify(@ModelAttribute("modifyBoard") NewsBoardDTO modifyBoard) throws Exception{
        newsboardService.modifyProcess(modifyBoard);
        return "redirect:/newsboard/info?newsIdx=" + modifyBoard.getNewsIdx();
    }


}
