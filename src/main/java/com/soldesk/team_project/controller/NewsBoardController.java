package com.soldesk.team_project.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.entity.NewsCategoryEntity;
import com.soldesk.team_project.infra.DriveUploader;
import com.soldesk.team_project.repository.NewsBoardRepository;
import com.soldesk.team_project.repository.NewsCategoryRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/newsBoard")
public class NewsBoardController {

    private final NewsBoardRepository newsBoardRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final DriveUploader driveUploader;

    @Value("${google.drive.newsboard-folder-id}")
    private String newsFolderId;

    private static final int CATEGORY_NOTICE = 1;
    private static final int CATEGORY_NEWS   = 2;
    private static final int CATEGORY_VIDEO  = 3;
    private static final int CATEGORY_COLUMN = 4;

    /* ================= 메인 ================= */
    @GetMapping("/main")
    public String newsMain(Model model) {

        List<NewsBoardEntity> newsList = newsBoardRepository
                .findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(2, 1)
                .stream().limit(3).collect(Collectors.toList());

        List<NewsBoardEntity> videoList = newsBoardRepository
                .findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(3, 1)
                .stream().limit(3).collect(Collectors.toList());

        List<NewsBoardEntity> columnList = newsBoardRepository
                .findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(4, 1)
                .stream().limit(3).collect(Collectors.toList());

        List<NewsBoardEntity> noticeList = newsBoardRepository
                .findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(1, 1)
                .stream().limit(3).collect(Collectors.toList());

        model.addAttribute("newsList", newsList);
        model.addAttribute("videoList", videoList);
        model.addAttribute("columnList", columnList);
        model.addAttribute("noticeList", noticeList);

        return "newsBoard/nMain";
    }

    /* ================= 리스트 ================= */
    @GetMapping("/list")
    public String list(@RequestParam(name = "category", defaultValue = "1") Integer categoryIdx,
                       @RequestParam(name = "page", defaultValue = "1") Integer page,
                       Model model) {

        int pageSize = 10;
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("newsIdx").descending());

        Page<NewsBoardEntity> boardPage =
                newsBoardRepository.findByCategoryCategoryIdxAndNewsActiveOrderByNewsIdxDesc(
                        categoryIdx, 1, pageable
                );

        int totalPages = boardPage.getTotalPages();
        int currentPage = page;

        int blockSize = 10;
        int startPage = ((currentPage - 1) / blockSize) * blockSize + 1;
        int endPage = Math.min(startPage + blockSize - 1, totalPages);

        model.addAttribute("boards", boardPage.getContent());
        model.addAttribute("page", boardPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("categoryIdx", categoryIdx);

        // 공지는 따로
        if (categoryIdx == CATEGORY_NOTICE) {
            return "newsBoard/noticeList";
        } else {
            return "newsBoard/nList";
        }
    }

    /* ================= 상세 ================= */
    @GetMapping("/detail")
    public String detail(@RequestParam("newsIdx") Integer newsIdx,
                         Model model) {

        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) {
            return "redirect:/newsBoard/list";
        }

        board.setNewsViews(board.getNewsViews() == null ? 1 : board.getNewsViews() + 1);
        newsBoardRepository.save(board);

        model.addAttribute("board", board);

        int cat = board.getCategory().getCategoryIdx();
        if (cat == CATEGORY_COLUMN) {
            return "newsBoard/cInfo";
        } else {
            return "newsBoard/nInfo";
        }
    }

    /* ================= 글쓰기 폼 ================= */
    @GetMapping("/write")
    public String writeForm(@RequestParam("category") Integer categoryIdx,
                            HttpSession session,
                            Model model) {

        if (!canWrite(categoryIdx, session)) {
            return "redirect:/newsBoard/list?category=" + categoryIdx;
        }

        NewsBoardDTO dto = new NewsBoardDTO();
        dto.setCategoryIdx(categoryIdx);
        model.addAttribute("news", dto);

        return "newsBoard/write";
    }

    /* ================= 글쓰기 처리 ================= */
    @PostMapping("/write")
    public String writeSubmit(@ModelAttribute("news") NewsBoardDTO dto,
                              HttpSession session,
                              Model model) {

        Integer categoryIdx = dto.getCategoryIdx();
        if (!canWrite(categoryIdx, session)) {
            return "redirect:/newsBoard/list?category=" + categoryIdx;
        }

        NewsBoardEntity entity = new NewsBoardEntity();
        entity.setNewsTitle(dto.getNewsTitle());
        entity.setNewsContent(dto.getNewsContent());
        entity.setNewsRegDate(LocalDate.now());
        entity.setNewsLike(0);
        entity.setNewsViews(0);
        entity.setNewsActive(1);

        // 파일 업로드 -> id만 저장
        var file = dto.getNewsBoardFile();
        if (file != null && !file.isEmpty()) {
            try {
                var info = driveUploader.upload(file, newsFolderId);

                entity.setFileAttached(1);
                entity.setStoredFileName(info.name());
                entity.setDriveFileId(info.fileId());     // ← 이것만 저장
                entity.setNewsImgPath(null);              // 화면에서 조립할 거니까 비워둠

            } catch (Exception e) {
                entity.setFileAttached(0);
            }
        } else {
            entity.setFileAttached(0);
        }

        NewsCategoryEntity category = newsCategoryRepository.findById(categoryIdx).orElse(null);
        entity.setCategory(category);

        AdminEntity loginAdmin = (AdminEntity) session.getAttribute("loginAdmin");
        LawyerEntity loginLawyer = (LawyerEntity) session.getAttribute("loginLawyer");

        if (categoryIdx == CATEGORY_COLUMN) {
            entity.setLawyer(loginLawyer);
        } else {
            entity.setAdmin(loginAdmin);
        }

        // 동영상이면 url + videoId 저장
        if (categoryIdx == CATEGORY_VIDEO) {
            entity.setVideoUrl(dto.getVideoUrl());
            entity.setVideoId(dto.getVideoId());
        }

        newsBoardRepository.save(entity);
        return "redirect:/newsBoard/list?category=" + categoryIdx;
    }

    /* ================= 수정 폼 ================= */
    @GetMapping("/modify")
    public String modifyForm(@RequestParam("newsIdx") Integer newsIdx,
                             HttpSession session,
                             Model model) {
        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) {
            return "redirect:/newsBoard/list";
        }

        if (!isOwner(board, session)) {
            return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
        }

        model.addAttribute("board", board);
        return "newsBoard/modify";
    }

    /* ================= 수정 처리 ================= */
    @PostMapping("/modify")
    public String modifySubmit(@RequestParam("newsIdx") Integer newsIdx,
                               @ModelAttribute NewsBoardDTO dto,
                               HttpSession session) {

        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) {
            return "redirect:/newsBoard/list";
        }
        if (!isOwner(board, session)) {
            return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
        }

        board.setNewsTitle(dto.getNewsTitle());
        board.setNewsContent(dto.getNewsContent());

        var file = dto.getNewsBoardFile();
        if (file != null && !file.isEmpty()) {
            try {
                var info = driveUploader.upload(file, newsFolderId);

                board.setFileAttached(1);
                board.setStoredFileName(info.name());
                board.setDriveFileId(info.fileId());    // ← 새 id 저장
                board.setNewsImgPath(null);             // 화면에서 조립

            } catch (Exception e) {
                // 실패 시 기존 이미지 유지
            }
        }

        if (board.getCategory().getCategoryIdx() == CATEGORY_VIDEO) {
            board.setVideoUrl(dto.getVideoUrl());
            board.setVideoId(dto.getVideoId());
        }

        newsBoardRepository.save(board);
        return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
    }

    /* ================= 삭제 ================= */
    @PostMapping("/delete")
    public String delete(@RequestParam("newsIdx") Integer newsIdx,
                         HttpSession session) {

        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null) {
            return "redirect:/newsBoard/list";
        }

        if (!isOwner(board, session)) {
            return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
        }

        board.setNewsActive(0);
        newsBoardRepository.save(board);

        Integer cat = board.getCategory() != null ? board.getCategory().getCategoryIdx() : 1;
        return "redirect:/newsBoard/list?category=" + cat;
    }

    /* ================= 좋아요 ================= */
    @PostMapping("/like")
    @ResponseBody
    public String like(@RequestParam("newsIdx") Integer newsIdx) {
        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) {
            return "FAIL";
        }
        int cur = board.getNewsLike() == null ? 0 : board.getNewsLike();
        board.setNewsLike(cur + 1);
        newsBoardRepository.save(board);
        return "OK";
    }

    /* ================= 권한 체크 ================= */
    private boolean canWrite(Integer categoryIdx, HttpSession session) {
        AdminEntity loginAdmin = (AdminEntity) session.getAttribute("loginAdmin");
        LawyerEntity loginLawyer = (LawyerEntity) session.getAttribute("loginLawyer");

        if (categoryIdx == CATEGORY_NOTICE) {
            return loginAdmin != null && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
        } else if (categoryIdx == CATEGORY_NEWS || categoryIdx == CATEGORY_VIDEO) {
            return loginAdmin != null && "reporter".equalsIgnoreCase(loginAdmin.getAdminRole());
        } else if (categoryIdx == CATEGORY_COLUMN) {
            return loginLawyer != null;
        }
        return false;
    }

    private boolean isOwner(NewsBoardEntity board, HttpSession session) {
        AdminEntity loginAdmin = (AdminEntity) session.getAttribute("loginAdmin");
        LawyerEntity loginLawyer = (LawyerEntity) session.getAttribute("loginLawyer");

        int cat = board.getCategory().getCategoryIdx();

        if (cat == CATEGORY_NOTICE || cat == CATEGORY_NEWS || cat == CATEGORY_VIDEO) {
            if (board.getAdmin() == null || loginAdmin == null) return false;
            return board.getAdmin().getAdminIdx().equals(loginAdmin.getAdminIdx());
        } else if (cat == CATEGORY_COLUMN) {
            if (board.getLawyer() == null || loginLawyer == null) return false;
            return board.getLawyer().getLawyerIdx().equals(loginLawyer.getLawyerIdx());
        }
        return false;
    }
}
