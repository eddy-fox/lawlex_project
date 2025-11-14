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
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.controller.MemberController.AdminSession;
import com.soldesk.team_project.controller.MemberController.LawyerSession;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.entity.NewsCategoryEntity;
import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.NewsBoardRepository;
import com.soldesk.team_project.repository.NewsCategoryRepository;
import com.soldesk.team_project.service.FirebaseStorageService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/newsBoard")
public class NewsBoardController {

    private final NewsBoardRepository newsBoardRepository;
    private final NewsCategoryRepository newsCategoryRepository;
    private final AdminRepository adminRepository;     // 세션→엔티티 변환용
    private final LawyerRepository lawyerRepository;   // 세션→엔티티 변환용
    private final FirebaseStorageService storageService;

    private static final int CATEGORY_NOTICE = 1;
    private static final int CATEGORY_NEWS   = 2;
    private static final int CATEGORY_VIDEO  = 3;
    private static final int CATEGORY_COLUMN = 4;

    /* ================ 메인 ================ */
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

    /* ================ 리스트 ================ */
    @GetMapping("/list")
    public String list(@RequestParam(name = "category", defaultValue = "1") Integer categoryIdx,
                       @RequestParam(name = "page", defaultValue = "1") Integer page,
                       Model model,
                       HttpSession session) {

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

        // 뷰에서 권한체크하려고 넣어주는 것
        model.addAttribute("loginAdmin", getLoginAdmin(session));
        model.addAttribute("loginLawyer", getLoginLawyer(session));

        if (categoryIdx == CATEGORY_NOTICE) {
            return "newsBoard/noticeList";
        } else {
            return "newsBoard/nList";
        }
    }

    /* ================ 상세 ================ */
    @GetMapping("/detail")
    public String detail(@RequestParam("newsIdx") Integer newsIdx, Model model, HttpSession session) {
        var board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null || board.getNewsActive() == 0) return "redirect:/newsBoard/list";

        board.setNewsViews(board.getNewsViews() == null ? 1 : board.getNewsViews() + 1);
        newsBoardRepository.save(board);

        // ✅ 항상 news_imgpath(=풀 URL) 우선 사용, 없으면 id로부터 생성
        String imgUrl = board.getNewsImgPath();
        if (imgUrl == null && board.getDriveFileId() != null) {
            imgUrl = storageService.buildPublicUrl(board.getDriveFileId()); // id → URL
        }

        model.addAttribute("board", board);
        model.addAttribute("imgUrl", imgUrl);
        model.addAttribute("loginAdmin", getLoginAdmin(session));
        model.addAttribute("loginLawyer", getLoginLawyer(session));
        return (board.getCategory().getCategoryIdx() == 4) ? "newsBoard/cInfo" : "newsBoard/nInfo";
}


    /* ================ 글쓰기 폼 ================ */
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

        return "newsBoard/newswrite";
    }

    private String nowUuidName(String originalFilename) {
    String ext = getExt(originalFilename);
    String now = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS"));
    String shortUuid = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    return now + "-" + shortUuid + ext; // 예: 20251113_213015123-7f3a9c1b.jpg
}

private String getExt(String original) {
    if (original == null || original.isBlank()) return ".bin";
    String name = original.trim();
    int i = name.lastIndexOf('.');
    if (i < 0 || i == name.length() - 1) return ".bin";
    String ext = name.substring(i).toLowerCase(java.util.Locale.ROOT);
    if (ext.length() > 10 || ext.contains("/") || ext.contains("\\") || ext.contains(" ")) return ".bin";
    return ext;
}
    /* ================ 글쓰기 처리 ================ */
    @PostMapping("/write")
public String writeSubmit(@ModelAttribute("news") NewsBoardDTO dto,
                          @RequestParam(value = "imgFile", required = false) MultipartFile imgFile,
                          HttpSession session) throws Exception {

    Integer categoryIdx = dto.getCategoryIdx();
    if (!canWrite(categoryIdx, session)) {
        return "redirect:/newsBoard/list?category=" + categoryIdx;
    }

    // 🔴 여기서 무조건 엔티티로 다시 꺼낸다
    AdminEntity loginAdmin = getLoginAdmin(session);   // 세션에 AdminSession 있어도 엔티티로 바꿔줄 거임
    LawyerEntity loginLawyer = getLoginLawyer(session); // 아래에 헬퍼 하나 더 만들거야

    NewsBoardEntity entity = new NewsBoardEntity();
    entity.setNewsTitle(dto.getNewsTitle());
    entity.setNewsContent(dto.getNewsContent());
    entity.setNewsRegDate(LocalDate.now());
    entity.setNewsLike(0);
    entity.setNewsViews(0);
    entity.setNewsActive(1);

    // 카테고리
    NewsCategoryEntity category = newsCategoryRepository.findById(categoryIdx).orElse(null);
    entity.setCategory(category);

    // ✨ 작성자 세팅
    if (categoryIdx == CATEGORY_COLUMN) {
        // 칼럼은 변호사
        entity.setLawyer(loginLawyer);
    } else {
        // 공지/뉴스/동영상은 관리자
        entity.setAdmin(loginAdmin);   // ← 이게 null 아니어야 admin_idx가 들어감
    }

    // 동영상이면 url
    if (categoryIdx == CATEGORY_VIDEO) {
        entity.setVideoUrl(dto.getVideoUrl());
    }


    if (imgFile != null && !imgFile.isEmpty()) {
    String filename = nowUuidName(imgFile.getOriginalFilename()); // 짧고 유니크한 이름
    String objectPath = "news/" + filename;                       // 저장 폴더(news) 지정

    var uploaded = storageService.upload(imgFile, objectPath);    // Firebase 업로드
    entity.setFileAttached(1);
    entity.setStoredFileName(filename);                           // 짧은 실제 파일명
    entity.setNewsImgPath(uploaded.url());                     // 예: news/2025...-abcd1234.jpg
    entity.setDriveFileId(uploaded.fileId());                        // 예: https://storage.googleapis.com/...
}
    newsBoardRepository.save(entity);
    return "redirect:/newsBoard/list?category=" + categoryIdx;
}


    /* ================ 수정 폼 ================ */
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
        return "newsBoard/newsmodify";
    }

    /* ================ 수정 처리 ================ */
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

        // var file = dto.getNewsBoardFile();
        // if (file != null && !file.isEmpty()) {
        //     try {
        //         var info = driveUploader.upload(file, newsFolderId);
        //         board.setFileAttached(1);
        //         board.setStoredFileName(info.name());
        //         board.setDriveFileId(info.fileId());
        //         board.setNewsImgPath(null);
        //     } catch (Exception e) {
        //         // 실패 시 기존 이미지 유지
        //     }
        // }

        var file = dto.getNewsBoardFile(); // 폼에서 넘어오는 MultipartFile
    if (file != null && !file.isEmpty()) {
    try {
        String filename = nowUuidName(file.getOriginalFilename());
        String objectPath = "news/" + filename;

        var uploaded = storageService.upload(file, objectPath);
        board.setFileAttached(1);
        board.setStoredFileName(filename);
        board.setNewsImgPath(uploaded.url());
        board.setDriveFileId(uploaded.fileId());
        // 필요 시 기존 드라이브 경로 무력화하고 싶으면 위처럼 새 값으로 덮어쓰기만 하면 됨
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

    /* ================ 삭제 ================ */
    @PostMapping("/delete")
    public String delete(@RequestParam("newsIdx") Integer newsIdx,
                         HttpSession session) {

        NewsBoardEntity board = newsBoardRepository.findById(newsIdx).orElse(null);
        if (board == null) {
            return "redirect:/newsBoard/list";
        }

        if (!isOwner(board, session)) {
            // 작성자가 아니면 그냥 상세로 돌려보내기
            return "redirect:/newsBoard/detail?newsIdx=" + newsIdx;
        }

        board.setNewsActive(0);          // 소프트 삭제
        newsBoardRepository.save(board);

        Integer cat = board.getCategory() != null
                ? board.getCategory().getCategoryIdx()
                : CATEGORY_NOTICE;

        return "redirect:/newsBoard/list?category=" + cat;
    }

    /* ================ 좋아요 ================ */
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

    /* ================ 권한 체크 ================ */
    private boolean canWrite(Integer categoryIdx, HttpSession session) {
    AdminEntity loginAdmin = getLoginAdmin(session);
    LawyerEntity loginLawyer = getLoginLawyer(session);

    if (categoryIdx == CATEGORY_NOTICE) {
        // 공지 : 관리자만
        return loginAdmin != null
                && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
    } else if (categoryIdx == CATEGORY_NEWS) {
        // 뉴스 : 기자
        return loginAdmin != null
                && "reporter".equalsIgnoreCase(loginAdmin.getAdminRole());
    } else if (categoryIdx == CATEGORY_VIDEO) {
        // 동영상 : 관리자
        return loginAdmin != null
                && "admin".equalsIgnoreCase(loginAdmin.getAdminRole());
    } else if (categoryIdx == CATEGORY_COLUMN) {
        // 칼럼 : 변호사
        return loginLawyer != null;
    }
    return false;
}


    private boolean isOwner(NewsBoardEntity board, HttpSession session) {
        int cat = board.getCategory().getCategoryIdx();

        // 칼럼 → 변호사
        if (cat == CATEGORY_COLUMN) {
            LawyerEntity loginLawyer = getLoginLawyer(session);
            return loginLawyer != null &&
                   board.getLawyer() != null &&
                   board.getLawyer().getLawyerIdx().equals(loginLawyer.getLawyerIdx());
        }

        // 나머지(공지/뉴스/동영상) → 관리자
        AdminEntity loginAdmin = getLoginAdmin(session);
        return loginAdmin != null &&
               board.getAdmin() != null &&
               board.getAdmin().getAdminIdx().equals(loginAdmin.getAdminIdx());
    }

    /* ================ 세션 → 엔티티 변환 헬퍼 ================ */
    private AdminEntity getLoginAdmin(HttpSession session) {
        Object obj = session.getAttribute("loginAdmin");
        if (obj == null) return null;

        if (obj instanceof AdminEntity ae) {
            return ae;
        }
        if (obj instanceof AdminSession as) {
            return adminRepository.findById(as.getAdminIdx()).orElse(null);
        }
        return null;
    }

    private LawyerEntity getLoginLawyer(HttpSession session) {
        Object obj = session.getAttribute("loginLawyer");
        if (obj == null) return null;

        if (obj instanceof LawyerEntity le) {
            return le;
        }
        if (obj instanceof LawyerSession ls) {
            // LawyerSession 은 네가 public 필드로 만들어놨으니까 이렇게 접근
            return lawyerRepository.findById(ls.lawyerIdx).orElse(null);
        }
        return null;
    }
}
