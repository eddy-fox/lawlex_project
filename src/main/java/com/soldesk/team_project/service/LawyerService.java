package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.dto.ReboardDTO;

import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.ReBoardEntity;

import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.UserMasterRepository;
import com.soldesk.team_project.repository.ReBoardRepository;

import com.soldesk.team_project.util.FileStorageService;
import com.soldesk.team_project.service.FirebaseStorageService;
import com.soldesk.team_project.service.CalendarService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LawyerService {

    private final LawyerRepository lawyerRepository;
    private final FileStorageService fileStorageService;
    private final FirebaseStorageService firebaseStorageService;
    private final PasswordEncoder passwordEncoder;

    private final UserMasterRepository userMasterRepository;
    private final InterestRepository interestRepository;
    private final MemberRepository memberRepository;

    private final ReBoardRepository reBoardRepository;
    private final CalendarService calendarService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    private LawyerDTO convertLawyerDTO(LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerIdx(lawyerEntity.getLawyerIdx());
        lawyerDTO.setLawyerId(lawyerEntity.getLawyerId());
        lawyerDTO.setLawyerPass(lawyerEntity.getLawyerPass());
        lawyerDTO.setLawyerName(lawyerEntity.getLawyerName());
        lawyerDTO.setLawyerIdnum(lawyerEntity.getLawyerIdnum());
        lawyerDTO.setLawyerEmail(lawyerEntity.getLawyerEmail());
        lawyerDTO.setLawyerPhone(lawyerEntity.getLawyerPhone());
        lawyerDTO.setLawyerAgree(lawyerEntity.getLawyerAgree());
        lawyerDTO.setLawyerNickname(lawyerEntity.getLawyerNickname());
        lawyerDTO.setLawyerAuth(lawyerEntity.getLawyerAuth());
        lawyerDTO.setLawyerAddress(lawyerEntity.getLawyerAddress());
        lawyerDTO.setLawyerTel(lawyerEntity.getLawyerTel());
        lawyerDTO.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
        lawyerDTO.setLawyerComment(lawyerEntity.getLawyerComment());
        lawyerDTO.setLawyerLike(lawyerEntity.getLawyerLike());
        lawyerDTO.setLawyerAnswerCnt(lawyerEntity.getLawyerAnswerCnt());
        lawyerDTO.setLawyerActive(lawyerEntity.getLawyerActive());
        lawyerDTO.setInterestIdx(lawyerEntity.getInterestIdx());
        if (lawyerEntity.getInterest() != null) {
            lawyerDTO.setInterestName(lawyerEntity.getInterest().getInterestName());
        }
        return lawyerDTO;
    }

    private LawyerEntity convertLawyerEntity (LawyerDTO lawyerDTO) {
        LawyerEntity lawyerEntity = new LawyerEntity();
        lawyerEntity.setLawyerIdx(lawyerDTO.getLawyerIdx());
        lawyerEntity.setLawyerId(lawyerDTO.getLawyerId());
        lawyerEntity.setLawyerPass(lawyerDTO.getLawyerPass());
        lawyerEntity.setLawyerName(lawyerDTO.getLawyerName());
        lawyerEntity.setLawyerIdnum(lawyerDTO.getLawyerIdnum());
        lawyerEntity.setLawyerEmail(lawyerDTO.getLawyerEmail());
        lawyerEntity.setLawyerPhone(lawyerDTO.getLawyerPhone());
        lawyerEntity.setLawyerAgree(lawyerDTO.getLawyerAgree());
        lawyerEntity.setLawyerNickname(lawyerDTO.getLawyerNickname());
        lawyerEntity.setLawyerAuth(lawyerDTO.getLawyerAuth());
        lawyerEntity.setLawyerAddress(lawyerDTO.getLawyerAddress());
        lawyerEntity.setLawyerTel(lawyerDTO.getLawyerTel());
        lawyerEntity.setLawyerImgPath(lawyerDTO.getLawyerImgPath());
        lawyerEntity.setLawyerComment(lawyerDTO.getLawyerComment());
        lawyerEntity.setLawyerLike(lawyerDTO.getLawyerLike());
        lawyerEntity.setLawyerAnswerCnt(lawyerDTO.getLawyerAnswerCnt());
        lawyerEntity.setInterestIdx(lawyerDTO.getInterestIdx());
        
        InterestEntity interestEntity = interestRepository.findById(lawyerDTO.getInterestIdx()).orElse(null);
        lawyerEntity.setInterest(interestEntity);

        return lawyerEntity;
    }

    // 전체 변호사 조회
    public List<LawyerDTO> getAllLawyer() {
        List<LawyerEntity> lawyerEntityList = lawyerRepository.findByLawyerActive(1);
        return lawyerEntityList.stream()
                .map(this::convertLawyerDTO)
                .collect(Collectors.toList());
    }

    // 태그 별 특정 변호사 검색
    public List<LawyerDTO> searchLawyers(String searchType, String keyword) {
        List<LawyerEntity> lawyerEntityList;

        switch (searchType) {
            case "idx":
                try {
                    int idx = Integer.parseInt(keyword);
                    lawyerEntityList = lawyerRepository.findByLawyerIdxAndLawyerActive(idx, 1);
                } catch (NumberFormatException e) {
                    lawyerEntityList = new ArrayList<>();
                }
                break;
            case "id":
                lawyerEntityList = lawyerRepository
                        .findByLawyerIdContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1);
                break;
            case "name":
                lawyerEntityList = lawyerRepository
                        .findByLawyerNameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1);
                break;
            case "idnum":
                lawyerEntityList = lawyerRepository
                        .findByLawyerIdnumContainingAndLawyerActiveOrderByLawyerIdnumAsc(keyword, 1);
                break;
            case "email":
                lawyerEntityList = lawyerRepository
                        .findByLawyerEmailContainingIgnoreCaseAndLawyerActiveOrderByLawyerEmailAsc(keyword, 1);
                break;
            case "phone":
                lawyerEntityList = lawyerRepository
                        .findByLawyerPhoneContainingAndLawyerActiveOrderByLawyerPhoneAsc(keyword, 1);
                break;
            case "nickname":
                lawyerEntityList = lawyerRepository
                        .findByLawyerNicknameContainingIgnoreCaseAndLawyerActiveOrderByLawyerNicknameAsc(keyword, 1);
                break;
            case "auth":
                try {
                    int auth = Integer.parseInt(keyword);
                    lawyerEntityList = lawyerRepository
                            .findByLawyerAuthAndLawyerActiveOrderByLawyerAuthAsc(auth, 1);
                } catch (NumberFormatException e) {
                    lawyerEntityList = new ArrayList<>();
                }
                break;
            case "address":
                lawyerEntityList = lawyerRepository
                        .findByLawyerAddressContainingIgnoreCaseAndLawyerActiveOrderByLawyerAddressAsc(keyword, 1);
                break;
            case "tel":
                lawyerEntityList = lawyerRepository
                        .findByLawyerTelContainingAndLawyerActiveOrderByLawyerTelAsc(keyword, 1);
                break;
            case "comment":
                lawyerEntityList = lawyerRepository
                        .findByLawyerCommentContainingIgnoreCaseAndLawyerActiveOrderByLawyerCommentAsc(keyword, 1);
                break;
            default:
                lawyerEntityList = lawyerRepository.findByLawyerActive(1);
                break;
        }
        return lawyerEntityList.stream()
                .map(this::convertLawyerDTO)
                .collect(Collectors.toList());
    }

    // 세션에서 로그인된 유저 가져오기
    private UserMasterDTO currentLoginUserOrThrow() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attrs.getRequest().getSession(false);
        if (session == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }
        Object obj = session.getAttribute("loginUser");
        if (obj instanceof UserMasterDTO u) {
            return u;
        }
        throw new IllegalStateException("세션에 로그인 정보가 없습니다.");
    }

    // 세션의 로그인 유저가 변호사일 때, 그 프로필 DTO 반환
    @Transactional(readOnly = true)
    public LawyerDTO getSessionLawyer() {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"LAWYER".equalsIgnoreCase(login.getRole())) {
            throw new IllegalStateException("변호사만 접근 가능합니다.");
        }
        var le = lawyerRepository.findById(login.getLawyerIdx())
                .orElseThrow(() -> new IllegalStateException("변호사 정보를 찾을 수 없습니다."));
        return convertLawyerDTO(le);
    }

    // ===== 기존 포털용 가입 메서드 (기존 코드 유지) =====
    @Transactional
    public void joinFromPortal(LawyerDTO dto) {
        // 아이디 중복 (멤버/로이어 전체에서 중복 불가)
        if (isUserIdDuplicate(dto.getLawyerId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화
        String encPass;
        if (notBlank(dto.getLawyerPass())) {
            encPass = passwordEncoder.encode(dto.getLawyerPass());
        } else {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        LawyerEntity le = LawyerEntity.builder()
                .lawyerId(dto.getLawyerId())
                .lawyerPass(encPass)
                .lawyerName(dto.getLawyerName())
                .lawyerEmail(dto.getLawyerEmail())
                .lawyerPhone(digits(dto.getLawyerPhone()))
                .lawyerTel(digits(dto.getLawyerTel()))
                .lawyerIdnum(digits(dto.getLawyerIdnum()))
                .lawyerAddress(dto.getLawyerAddress())
                .lawyerNickname(dto.getLawyerNickname())
                .interestIdx(dto.getInterestIdx())   // 로이어는 단일 관심사 사용
                .lawyerActive(1)                     // 활성
                .lawyerAuth(0)                       // 기본 미승인(필요시)
                .build();

        lawyerRepository.save(le);
    }


    // === 새 변호사 회원가입 (LawyerController에서 사용) ===
    @Transactional
    public void joinLawyer(LawyerDTO dto, MultipartFile certImage, MultipartFile lawyerImage, String availabilityJson) {

        // 1) 아이디 중복 체크 (멤버/변호사 모두 포함)
        if (isUserIdDuplicate(dto.getLawyerId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2) 비밀번호 필수 + 암호화
        if (!notBlank(dto.getLawyerPass())) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }
        String encPass = passwordEncoder.encode(dto.getLawyerPass());

        Integer interest = dto.getInterestIdx();
        if (interest == null) {
            interest = 1;
        }

        // 3) 변호사 사진 업로드 처리 (Firebase Storage 사용)
        String lawyerImgPath = null;
        if (lawyerImage != null && !lawyerImage.isEmpty()) {
            try {
                // 파일명: 20251113_213015123-7f3a9c1b.jpg 형식
                String filename = nowUuidName(lawyerImage.getOriginalFilename());
                String objectPath = "lawyerprofile/" + filename;
                
                var uploaded = firebaseStorageService.upload(lawyerImage, objectPath);
                lawyerImgPath = uploaded.url(); // Firebase 공개 URL 저장
            } catch (Exception e) {
                throw new IllegalArgumentException("변호사 사진 업로드 중 오류가 발생했습니다: " + e.getMessage());
            }
        }

        // 수신동의: "1" 또는 "0"으로 저장 (동의하면 "1", 아니면 "0")
        String agreeValue = (dto.getLawyerAgree() != null && "1".equals(dto.getLawyerAgree())) ? "1" : "0";
        
        LawyerEntity le = LawyerEntity.builder()
                .lawyerId(dto.getLawyerId())
                .lawyerPass(encPass)
                .lawyerName(dto.getLawyerName())
                .lawyerIdnum(digits(dto.getLawyerIdnum()))
                .lawyerEmail(dto.getLawyerEmail())
                .lawyerPhone(digits(dto.getLawyerPhone()))   // 휴대폰
                .lawyerTel(digits(dto.getLawyerTel()))       // 사무실 전화
                .lawyerAddress(dto.getLawyerAddress())
                .lawyerNickname(dto.getLawyerNickname())
                .interestIdx(interest)
                .lawyerComment(dto.getLawyerComment())       // 한줄소개
                .lawyerImgPath(lawyerImgPath)                // 변호사 사진 경로
                .lawyerAgree(agreeValue)                     // 수신동의 (0 또는 1)
                .lawyerActive(1)
                .lawyerAuth(1)
                .build();

        lawyerRepository.save(le);
        
        // 4) 상담 가능 시간 저장
        if (availabilityJson != null && !availabilityJson.trim().isEmpty()) {
            try {
                JsonNode json = objectMapper.readTree(availabilityJson);
                List<Map<String, Object>> timeSlots = new ArrayList<>();

                if (json.isArray()) {
                    for (JsonNode slot : json) {
                        // lJoin.js에서는 "days"로 보내지만, lModify.js에서는 "weekdays"로 보냄
                        JsonNode weekdaysNode = slot.has("weekdays") ? slot.get("weekdays") : slot.get("days");
                        if (weekdaysNode == null || !weekdaysNode.isArray()) continue;
                        
                        List<Integer> weekdays = new ArrayList<>();
                        for (JsonNode wd : weekdaysNode) {
                            if (wd.isInt()) {
                                weekdays.add(wd.asInt());
                            } else if (wd.isTextual()) {
                                try {
                                    weekdays.add(Integer.parseInt(wd.asText()));
                                } catch (NumberFormatException e) {
                                    // 무시
                                }
                            }
                        }
                        
                        String startHHmm = slot.get("start").asText();
                        String endHHmm = slot.get("end").asText();
                        
                        if (!weekdays.isEmpty() && startHHmm != null && endHHmm != null) {
                            Map<String, Object> timeSlot = new HashMap<>();
                            timeSlot.put("weekdays", weekdays);
                            timeSlot.put("start", startHHmm);
                            timeSlot.put("end", endHHmm);
                            timeSlots.add(timeSlot);
                        }
                    }
                }

                if (!timeSlots.isEmpty()) {
                    calendarService.updateAvailabilityMultiple(le.getLawyerIdx(), timeSlots);
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 무시 (상담 시간 없이 가입 가능)
                e.printStackTrace();
            }
        }
    }



    // 변호사 프로필 수정
    @Transactional
    public LawyerUpdateResult updateProfileFromPortal(
            LawyerDTO dto,
            String newPassword,
            String confirmPassword,
            Long /*unused*/ userIdx,
            Integer lawyerIdx
    ) {
        LawyerEntity le = lawyerRepository.findById(lawyerIdx)
                .orElseThrow(() -> new IllegalArgumentException("변호사 계정을 찾을 수 없습니다."));

        // 아이디 변경
        if (notBlank(dto.getLawyerId()) && !dto.getLawyerId().equals(le.getLawyerId())) {
            if (isUserIdDuplicate(dto.getLawyerId())) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }
            le.setLawyerId(dto.getLawyerId());
        }

        // 비밀번호 변경
        if (notBlank(newPassword) || notBlank(confirmPassword)) {
            if (!notBlank(newPassword) || !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            le.setLawyerPass(passwordEncoder.encode(newPassword));
        }

        // 기본 프로필 갱신
        if (notBlank(dto.getLawyerName()))     le.setLawyerName(dto.getLawyerName());
        if (notBlank(dto.getLawyerEmail()))    le.setLawyerEmail(dto.getLawyerEmail());
        if (notBlank(dto.getLawyerNickname())) le.setLawyerNickname(dto.getLawyerNickname());
        if (notBlank(dto.getLawyerAddress()))  le.setLawyerAddress(dto.getLawyerAddress());
        if (dto.getInterestIdx() != null) {
            le.setInterestIdx(dto.getInterestIdx());
            var interest = interestRepository.findById(dto.getInterestIdx())
                    .orElse(null);
            if (interest != null) {
                le.setInterest(interest);
            }
        }

        if (notBlank(dto.getLawyerPhone())) le.setLawyerPhone(digits(dto.getLawyerPhone()));
        if (notBlank(dto.getLawyerTel()))   le.setLawyerTel(digits(dto.getLawyerTel()));
        if (notBlank(dto.getLawyerIdnum())) le.setLawyerIdnum(digits(dto.getLawyerIdnum()));

        lawyerRepository.save(le);

        return new LawyerUpdateResult(le.getLawyerId(), le);
    }

    // 중복 체크 (멤버/로이어 통합)
    public boolean isUserIdDuplicate(String userId) {
        boolean memberDup = memberRepository.existsByMemberId(userId);
        boolean lawyerDup = lawyerRepository.existsByLawyerId(userId);
        return memberDup || lawyerDup;
    }

    // 유틸
    private static String digits(String s) {
        return s == null ? null : s.replaceAll("\\D", "");
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    // 파일명 생성 (Firebase Storage용)
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

    // 결과 DTO
    public record LawyerUpdateResult(String newUserId, LawyerEntity lawyer) {}
    
    // OAuth2 변호사 회원가입
    @Transactional
    public LawyerEntity joinOAuthLawyer(TemporaryOauthDTO temp, LawyerDTO joinLawyer) {
        LawyerEntity lawyerEntity = convertLawyerEntity(joinLawyer);
        lawyerEntity.setLawyerId(temp.getProvider() + temp.getProviderId());
        lawyerEntity.setLawyerPass(temp.getProviderId() + temp.getEmail());
        lawyerEntity.setLawyerName(temp.getName());
        lawyerEntity.setLawyerEmail(temp.getEmail());
        lawyerEntity.setLawyerActive(1);
        lawyerEntity.setLawyerLike(0);
        lawyerEntity.setLawyerAnswerCnt(0);
        lawyerEntity.setLawyerProvider(temp.getProvider());
        lawyerEntity.setLawyerProviderId(temp.getProviderId());
        
        // 수신동의: "1" 또는 "0"으로 저장 (동의하면 "1", 아니면 "0")
        String agreeValue = (joinLawyer.getLawyerAgree() != null && "1".equals(joinLawyer.getLawyerAgree())) ? "1" : "0";
        lawyerEntity.setLawyerAgree(agreeValue);

        return lawyerRepository.save(lawyerEntity);
    }

    // 로그인 아이디로 변호사 한 명 가져오기
    public LawyerEntity getLawyer(String lawyerId) {
        return lawyerRepository.findByLawyerId(lawyerId)
                .orElseThrow(() -> new DataNotFoundException("변호사를 찾을 수 없습니다."));
    }

    // 문의 상세 조회 필요한 id 와 name
    public LawyerDTO qLawyerInquiry(Integer lawyerIdx) {
        LawyerEntity lawyerEntity = lawyerRepository.findById(lawyerIdx).orElse(null);
        return convertLawyerDTO(lawyerEntity);
    }

    // 변호사 리보드(내가 쓴 글) 조회
    @Transactional(readOnly = true)
    public List<ReboardDTO> getMyReboardsForLawyer(Integer lawyerIdx) {
        if (lawyerIdx == null) {
            return java.util.Collections.emptyList();
        }

        return reBoardRepository
                .findTop5ByLawyerIdxOrderByReboardRegDateDesc(lawyerIdx)
                .stream()
                .map(this::convertReboardDTO)
                .collect(Collectors.toList());
    }

    // ReBoardEntity -> ReboardDTO 변환
    private ReboardDTO convertReboardDTO(ReBoardEntity entity) {
        if (entity == null) return null;

    ReboardDTO dto = new ReboardDTO();
    dto.setReboardIdx(entity.getReboardIdx());
    dto.setReboardTitle(entity.getReboardTitle());
    dto.setReboardContent(entity.getReboardContent());
    dto.setReboardRegDate(entity.getReboardRegDate());

    // BoardEntity → boardIdx (PK) 및 boardTitle 꺼내서 넣기
    if (entity.getBoardEntity() != null) {
        dto.setBoardIdx(entity.getBoardEntity().getBoardIdx());
        dto.setBoardTitle(entity.getBoardEntity().getBoardTitle());
    } else {
        dto.setBoardIdx(null);
        dto.setBoardTitle(null);
    }

    if (entity.getLawyerIdx() != null) {
        dto.setLawyerIdx(entity.getLawyer().getLawyerIdx());
    } else {
        dto.setLawyerIdx(null);
    }

    return dto;
}

    // ===== 변호사 정보 수정 (세션 기반) =====

    // 변호사 프로필 수정
    @Transactional
    public void updateProfileForCurrent(LawyerDTO dto, MultipartFile lawyerImage, String calendarJson) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"LAWYER".equalsIgnoreCase(login.getRole())) {
            throw new IllegalStateException("변호사만 접근 가능합니다.");
        }
        
        LawyerEntity le = lawyerRepository.findById(login.getLawyerIdx())
                .orElseThrow(() -> new IllegalStateException("변호사 정보를 찾을 수 없습니다."));

        // 닉네임
        if (notBlank(dto.getLawyerNickname())) {
            le.setLawyerNickname(dto.getLawyerNickname());
        }

        // 이메일
        if (notBlank(dto.getLawyerEmail())) {
            le.setLawyerEmail(dto.getLawyerEmail());
        }

        // 사무실 주소
        if (notBlank(dto.getLawyerAddress())) {
            le.setLawyerAddress(dto.getLawyerAddress());
        }

        // 전문분야
        if (dto.getInterestIdx() != null) {
            le.setInterestIdx(dto.getInterestIdx());
            var interest = interestRepository.findById(dto.getInterestIdx()).orElse(null);
            if (interest != null) {
                le.setInterest(interest);
            }
        }

        // 한줄소개
        if (dto.getLawyerComment() != null) {
            le.setLawyerComment(dto.getLawyerComment());
        }

        // 사진 업로드
        if (lawyerImage != null && !lawyerImage.isEmpty()) {
            try {
                String filename = nowUuidName(lawyerImage.getOriginalFilename());
                String objectPath = "lawyerprofile/" + filename;
                var uploaded = firebaseStorageService.upload(lawyerImage, objectPath);
                le.setLawyerImgPath(uploaded.url());
            } catch (Exception e) {
                throw new IllegalArgumentException("변호사 사진 업로드 중 오류가 발생했습니다: " + e.getMessage());
            }
        }

        lawyerRepository.save(le);

        // 상담 가능 요일 및 시간대 업데이트
        if (calendarJson != null && !calendarJson.trim().isEmpty()) {
            try {
                JsonNode json = objectMapper.readTree(calendarJson);
                List<Map<String, Object>> timeSlots = new ArrayList<>();

                if (json.isArray()) {
                    for (JsonNode slot : json) {
                        if (slot.has("weekdays") && slot.has("start") && slot.has("end")) {
                            JsonNode weekdaysNode = slot.get("weekdays");
                            List<Integer> weekdays = new ArrayList<>();
                            
                            if (weekdaysNode.isArray()) {
                                for (JsonNode wd : weekdaysNode) {
                                    if (wd.isInt()) {
                                        weekdays.add(wd.asInt());
                                    }
                                }
                            }
                            
                            String startHHmm = slot.get("start").asText();
                            String endHHmm = slot.get("end").asText();
                            
                            if (!weekdays.isEmpty() && startHHmm != null && endHHmm != null) {
                                Map<String, Object> timeSlot = new HashMap<>();
                                timeSlot.put("weekdays", weekdays);
                                timeSlot.put("start", startHHmm);
                                timeSlot.put("end", endHHmm);
                                timeSlots.add(timeSlot);
                            }
                        }
                    }
                }

                // 빈 배열이어도 소프트 삭제를 위해 호출 (모든 일정을 active=0으로 변경)
                calendarService.updateAvailabilityMultiple(login.getLawyerIdx(), timeSlots);
            } catch (Exception e) {
                // JSON 파싱 실패 시 무시 (기존 일정 유지)
                e.printStackTrace();
            }
        }
    }

    // 변호사 비밀번호 변경 (아이디+전화번호+생년월일 검증)
    @Transactional
    public String changePasswordWithVerificationForCurrent(String lawyerId, String lawyerPhone, String lawyerIdnum,
                                                           String newPassword, String confirmPassword) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"LAWYER".equalsIgnoreCase(login.getRole())) {
            return "FAIL";
        }

        String phone = digits(lawyerPhone);
        String idnum = digits(lawyerIdnum);

        LawyerEntity le = lawyerRepository.findById(login.getLawyerIdx())
                .orElseThrow(() -> new IllegalStateException("변호사 정보를 찾을 수 없습니다."));

        // 본인 확인: 아이디, 전화번호, 생년월일 일치 확인
        boolean verified = Objects.equals(lawyerId, le.getLawyerId())
                        && Objects.equals(phone, le.getLawyerPhone())
                        && Objects.equals(idnum, le.getLawyerIdnum());
        
        if (!verified) {
            return "FAIL";
        }

        // 비밀번호 확인 일치 체크
        if (!notBlank(newPassword) || !newPassword.equals(confirmPassword)) {
            return "MISMATCH";
        }

        // 비밀번호 변경
        le.setLawyerPass(passwordEncoder.encode(newPassword));
        lawyerRepository.save(le);

        return "OK";
    }

    // 변호사 회원 탈퇴 (전화번호+생년월일 검증)
    @Transactional
    public boolean deactivateWithVerificationForCurrent(String lawyerPhone, String lawyerIdnum) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"LAWYER".equalsIgnoreCase(login.getRole())) {
            return false;
        }

        String phone = digits(lawyerPhone);
        String idnum = digits(lawyerIdnum);

        LawyerEntity le = lawyerRepository.findById(login.getLawyerIdx())
                .orElseThrow(() -> new IllegalStateException("변호사 정보를 찾을 수 없습니다."));

        boolean verified = Objects.equals(phone, le.getLawyerPhone())
                        && Objects.equals(idnum, le.getLawyerIdnum());
        
        if (!verified) {
            return false;
        }

        le.setLawyerActive(0);
        lawyerRepository.save(le);
        return true;
    }

}
