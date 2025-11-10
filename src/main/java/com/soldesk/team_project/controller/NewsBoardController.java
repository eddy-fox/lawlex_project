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

    // 카테고리 번호 고정
    private static final int CATEGORY_NOTICE = 1;   // 공지
    private static final int CATEGORY_NEWS = 2;     // 뉴스
    private static final int CATEGORY_VIDEO = 3;    // 동영상
    private static final int CATEGORY_COLUMN = 4;   // 칼럼

    // 메인: /newsBoard/main
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

    // 리스트: /newsBoard/list?category=...
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

        if (categoryIdx == CATEGORY_NOTICE) {
            return "newsBoard/noticeList";
        } else {
            return "newsBoard/nList";
        }
    }

    // 상세보기
    @GetMapping("/detail")
    public String detail(@RequestParam("newsIdx") Integer newsIdx,
                         Model model) {

        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) {
            return "redirect:/newsBoard/list";
        }

        // 조회수 증가
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

    // 글쓰기 폼
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

    // 글쓰기 처리
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

        // 파일 업로드
        org.springframework.web.multipart.MultipartFile file = dto.getNewsBoardFile();
        if (file != null && !file.isEmpty()) {
            try {
                DriveUploader.UploadedFileInfo info = driveUploader.upload(file, newsFolderId);

                entity.setFileAttached(1);
                entity.setStoredFileName(info.name());
                // 여기서부터 중요한 부분: 드라이브 썸네일 URL로 저장
                entity.setNewsImgPath("https://drive.google.com/thumbnail?id=" + info.fileId() + "&sz=w1000");
                entity.setDriveFileId(info.fileId());

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

        if (categoryIdx == CATEGORY_VIDEO) {
            entity.setVideoUrl(dto.getVideoUrl());
        }

        newsBoardRepository.save(entity);
        return "redirect:/newsBoard/list?category=" + categoryIdx;
    }

    // 수정 폼
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

    // 수정 처리
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

        // 새 이미지가 올라왔을 때만 교체
        org.springframework.web.multipart.MultipartFile file = dto.getNewsBoardFile();
        if (file != null && !file.isEmpty()) {
            try {
                DriveUploader.UploadedFileInfo info = driveUploader.upload(file, newsFolderId);

                board.setFileAttached(1);
                board.setStoredFileName(info.name());
                board.setNewsImgPath("https://drive.google.com/thumbnail?id=" + info.fileId() + "&sz=w1000");
                board.setDriveFileId(info.fileId());

            } catch (Exception e) {
                // 실패 시 기존 이미지 유지
            }
        }

        if (board.getCategory().getCategoryIdx() == CATEGORY_VIDEO) {
            board.setVideoUrl(dto.getVideoUrl());
        }

        newsBoardRepository.save(board);
        return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
    }

    // 삭제 (soft delete)
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

    // 글쓰기 권한
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

    // 수정/삭제 권한 (본인만)
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
