package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.dto.UserMasterDTO;
import com.soldesk.team_project.dto.BoardDTO;
import com.soldesk.team_project.dto.CommentDTO;
import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.CommentEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.repository.CommentRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final LawyerRepository lawyerRepository;

    // [ADD] 게시글/댓글용 리포지토리
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final com.soldesk.team_project.repository.NewsBoardRepository newsBoardRepository;

    private MemberDTO convertMemberDTO (MemberEntity memberEntity) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberIdx(memberEntity.getMemberIdx());
        memberDTO.setMemberId(memberEntity.getMemberId());
        memberDTO.setMemberPass(memberEntity.getMemberPass());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberIdnum(memberEntity.getMemberIdnum());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberPhone(memberEntity.getMemberPhone());
        memberDTO.setMemberAgree(memberEntity.getMemberAgree());
        memberDTO.setMemberNickname(memberEntity.getMemberNickname());
        memberDTO.setMemberActive(memberEntity.getMemberActive());
        memberDTO.setMemberPoint(memberEntity.getMemberPoint());
        memberDTO.setMemberProvider(memberEntity.getMemberProvider());
        memberDTO.setMemberProviderId(memberEntity.getMemberProviderId());
        // interestIdx1, interestIdx2, interestIdx3 모두 설정
        memberDTO.setInterestIdx1(memberEntity.getInterestIdx1());
        memberDTO.setInterestIdx2(memberEntity.getInterestIdx2());
        memberDTO.setInterestIdx3(memberEntity.getInterestIdx3());
        
        // 관심분야 이름 합쳐서 설정 (3개)
        List<String> interestNames = new ArrayList<>();
        if (memberEntity.getInterestIdx1() != null) {
            interestRepository.findById(memberEntity.getInterestIdx1())
                .ifPresent(interest -> interestNames.add(interest.getInterestName()));
        }
        if (memberEntity.getInterestIdx2() != null) {
            interestRepository.findById(memberEntity.getInterestIdx2())
                .ifPresent(interest -> interestNames.add(interest.getInterestName()));
        }
        if (memberEntity.getInterestIdx3() != null) {
            interestRepository.findById(memberEntity.getInterestIdx3())
                .ifPresent(interest -> interestNames.add(interest.getInterestName()));
        }
        memberDTO.setInterestName(String.join(", ", interestNames));
        
        return memberDTO;
    }

    private MemberEntity convertMemberEntity (MemberDTO memberDTO) {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setMemberIdx(memberDTO.getMemberIdx());
        memberEntity.setMemberId(memberDTO.getMemberId());
        memberEntity.setMemberPass(memberDTO.getMemberPass());
        memberEntity.setMemberName(memberDTO.getMemberName());
        memberEntity.setMemberIdnum(memberDTO.getMemberIdnum());
        memberEntity.setMemberEmail(memberDTO.getMemberEmail());
        memberEntity.setMemberPhone(memberDTO.getMemberPhone());
        memberEntity.setMemberAgree(memberDTO.getMemberAgree());
        memberEntity.setMemberNickname(memberDTO.getMemberNickname());
        memberEntity.setMemberActive(memberDTO.getMemberActive());
        memberEntity.setMemberPoint(memberDTO.getMemberPoint());
        memberEntity.setMemberProvider(memberDTO.getMemberProvider());
        memberEntity.setMemberProviderId(memberDTO.getMemberProviderId());
        memberEntity.setInterestIdx1(memberDTO.getInterestIdx1());

        // InterestEntity interestEntity = interestRepository.findById(memberDTO.getInterestIdx1()).orElse(null);

        return memberEntity;
    }


    // 전체 회원 조회
    public List<MemberDTO> getAllMember() {
        List<MemberEntity> memberEntityList = memberRepository.findByMemberActive(1);
        return memberEntityList.stream()
            .map(memberEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 회원 검색
    public List<MemberDTO> searchMembers(String searchType, String keyword) {
        List<MemberEntity> memberEntityList;

        switch (searchType) {
            case "idx":
                try {
                    int idx = Integer.parseInt(keyword);
                    memberEntityList = memberRepository.findByMemberIdxAndMemberActive(idx, 1);
                } catch (NumberFormatException e) {
                    memberEntityList = new ArrayList<>();
                }
                break;
            case "id":
                memberEntityList = memberRepository
                    .findByMemberIdContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(keyword, 1);
                break;
            case "name":
                memberEntityList = memberRepository
                    .findByMemberNameContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(keyword, 1);
                break;
            case "idnum":
                memberEntityList = memberRepository
                    .findByMemberIdnumContainingAndMemberActiveOrderByMemberIdnumAsc(keyword, 1);
                break;
            case "email":
                memberEntityList = memberRepository
                    .findByMemberEmailContainingIgnoreCaseAndMemberActiveOrderByMemberEmailAsc(keyword, 1);
                break;
            case "phone":
                memberEntityList = memberRepository
                    .findByMemberPhoneContainingAndMemberActiveOrderByMemberPhoneAsc(keyword, 1);
                break;
            case "nickname":
                memberEntityList = memberRepository
                    .findByMemberNicknameContainingIgnoreCaseAndMemberActiveOrderByMemberNicknameAsc(keyword, 1);
                break;
            default:
                memberEntityList = memberRepository.findByMemberActive(1);
                break;
        }

        return memberEntityList.stream()
            .map(memberEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }

    // 세션에서 가져온 회원 검색
    public MemberDTO searchSessionMember(Integer memberIdx) {
        if (memberIdx == null) return null;
        return memberRepository.findById(memberIdx)
                .map(this::convertMemberDTO)
                .orElse(null);
    }

    // 관리자가 회원 정보 조회 (memberIdx로)
    public MemberDTO getMemberByIdx(int memberIdx) {
        MemberEntity memberEntity = memberRepository.findById(memberIdx).orElse(null);
        if (memberEntity == null) return null;
        return convertMemberDTO(memberEntity);
    }

    //특정 회원 검색
    public MemberEntity getMember(String memberName) {
        Optional<MemberEntity> member = this.memberRepository.findByMemberName(memberName);
        if (member.isPresent()) {
            return member.get();
        } else {
            throw new DataNotFoundException("member not found");
        }
    }

    // 공통 유틸 
    private static String digits(String s) { return s == null ? null : s.replaceAll("\\D", ""); }
    
    // 전화번호 포맷팅 (010-1234-5678 형식)
    private static String formatPhone(String s) {
        if (s == null) return null;
        String d = digits(s);
        if (d == null || d.length() < 10) return d;
        if (d.length() == 10) {
            return d.substring(0, 3) + "-" + d.substring(3, 6) + "-" + d.substring(6);
        }
        if (d.length() == 11) {
            return d.substring(0, 3) + "-" + d.substring(3, 7) + "-" + d.substring(7);
        }
        return d;
    }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    private static Integer nz(Integer v) { return v == null ? -1 : v; }
    private static void validateDistinctInterests(MemberDTO d) {
        Integer a = nz(d.getInterestIdx1()), b = nz(d.getInterestIdx2()), c = nz(d.getInterestIdx3());
        if (a.equals(b) || a.equals(c) || b.equals(c)) {
            throw new IllegalArgumentException("관심 분야는 서로 다른 항목으로 선택해주세요.");
        }
    }

    // 아이디 중복 체크 
    public boolean isUserIdDuplicate(String userId) {
        boolean memberDup = memberRepository.existsByMemberId(userId);
        boolean lawyerDup = lawyerRepository.existsByLawyerId(userId);
        return memberDup || lawyerDup;
    }

    // 일반 회원가입 (항상 BCrypt 저장)
    @Transactional
    public void joinNormal(MemberDTO dto) {
        if (isUserIdDuplicate(dto.getMemberId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        validateDistinctInterests(dto);

        String enc = passwordEncoder.encode(dto.getMemberPass());

        // 수신동의: "1" 또는 "0"으로 저장 (동의하면 "1", 아니면 "0")
        String agreeValue = ("1".equals(dto.getMemberAgree())) ? "1" : "0";
        
        MemberEntity me = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPass(enc)
                .memberName(dto.getMemberName())
                .memberEmail(dto.getMemberEmail())
                .memberPhone(formatPhone(dto.getMemberPhone()))
                .memberIdnum(digits(dto.getMemberIdnum()))
                .memberNickname(dto.getMemberNickname())
                .memberAgree(agreeValue)
                .memberActive(1)
                .memberProvider("local")
                // 호환 컬럼
                .interestIdx(dto.getInterestIdx1())
                .interestIdx1(dto.getInterestIdx1())
                .interestIdx2(dto.getInterestIdx2())
                .interestIdx3(dto.getInterestIdx3())
                .build();

        memberRepository.save(me);
    }

    // 일반회원 프로필 수정 세션의 memberIdx로 검증
    @Transactional
    public MemberUpdateResult updateMemberProfile(
            MemberDTO dto,
            String newPassword,
            String confirmPassword,
            Long ignoredUserIdx,
            Integer memberIdx
    ) {
        if (memberIdx == null) throw new IllegalArgumentException("본인만 수정할 수 있습니다.");

        MemberEntity me = memberRepository.findById(memberIdx)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (notBlank(dto.getMemberId()) && !dto.getMemberId().equals(me.getMemberId())) {
            if (isUserIdDuplicate(dto.getMemberId())) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }
            me.setMemberId(dto.getMemberId());
        }

        if (notBlank(newPassword) || notBlank(confirmPassword)) {
            if (!notBlank(newPassword) || !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            me.setMemberPass(passwordEncoder.encode(newPassword));
        }

        validateDistinctInterests(dto);
        if (dto.getMemberNickname() != null) me.setMemberNickname(dto.getMemberNickname());
        if (dto.getMemberEmail() != null)    me.setMemberEmail(dto.getMemberEmail());
        if (dto.getInterestIdx1() != null)   me.setInterestIdx1(dto.getInterestIdx1());
        if (dto.getInterestIdx2() != null)   me.setInterestIdx2(dto.getInterestIdx2());
        if (dto.getInterestIdx3() != null)   me.setInterestIdx3(dto.getInterestIdx3());

        memberRepository.save(me);

        return new MemberUpdateResult(me.getMemberId(), me);
    }

    // 서비스 내부에서 세션의 loginUser(UserMasterDTO) 꺼내기
    private UserMasterDTO currentLoginUserOrThrow() {
        ServletRequestAttributes attrs =
            (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpSession session = attrs.getRequest().getSession(false);
        if (session == null) throw new IllegalStateException("로그인이 필요합니다.");
        Object obj = session.getAttribute("loginUser");
        if (obj instanceof UserMasterDTO u) return u;
        throw new IllegalStateException("세션에 로그인 정보가 없습니다.");
    }

    // 세션의 로그인 유저가 일반회원일 때, 그 프로필 DTO 반환
    @Transactional(readOnly = true)
    public MemberDTO getSessionMember() {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"MEMBER".equalsIgnoreCase(login.getRole())) {
            throw new IllegalStateException("일반회원만 접근 가능합니다.");
        }
        return memberRepository.findById(login.getMemberIdx())
                .map(this::convertMemberDTO)
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
    }

    // 아이디 찾기 member → lawyer 순서
    public String findId(String memberPhone, String memberIdnum) {
        String phoneDigits = digits(memberPhone);
        String idnumDigits = digits(memberIdnum);

        if (phoneDigits == null || idnumDigits == null) {
            return "NOT_FOUND";
        }

        // 여러 형식으로 시도: formatted, phoneDigits, 그리고 모든 회원을 가져와서 digits로 비교
        Optional<MemberEntity> memOpt = Optional.empty();
        
        // 1. 포맷된 형식으로 시도 (010-1234-5678)
        String formatted = formatPhone(phoneDigits);
        if (formatted != null) {
            memOpt = memberRepository.findByMemberPhoneAndMemberIdnum(formatted, idnumDigits);
        }
        
        // 2. 숫자만으로 시도 (01012345678)
        if (memOpt.isEmpty()) {
            memOpt = memberRepository.findByMemberPhoneAndMemberIdnum(phoneDigits, idnumDigits);
        }
        
        // 3. 모든 활성 회원을 가져와서 digits로 비교 (DB에 저장된 형식이 다를 경우 대비)
        if (memOpt.isEmpty()) {
            List<MemberEntity> allMembers = memberRepository.findByMemberActive(1);
            for (MemberEntity member : allMembers) {
                if (Objects.equals(phoneDigits, digits(member.getMemberPhone())) 
                    && Objects.equals(idnumDigits, digits(member.getMemberIdnum()))) {
                    memOpt = Optional.of(member);
                    break;
                }
            }
        }
        
        if (memOpt.isPresent()) {
            return memOpt.get().getMemberId();
        }

        // 변호사도 동일하게 처리
        Optional<LawyerEntity> lawOpt = Optional.empty();
        
        // 1. 포맷된 형식으로 시도
        if (formatted != null) {
            lawOpt = lawyerRepository.findByLawyerPhoneAndLawyerIdnum(formatted, idnumDigits);
        }
        
        // 2. 숫자만으로 시도
        if (lawOpt.isEmpty()) {
            lawOpt = lawyerRepository.findByLawyerPhoneAndLawyerIdnum(phoneDigits, idnumDigits);
        }
        
        // 3. 모든 활성 변호사를 가져와서 digits로 비교
        if (lawOpt.isEmpty()) {
            List<LawyerEntity> allLawyers = lawyerRepository.findByLawyerActive(1);
            for (LawyerEntity lawyer : allLawyers) {
                if (Objects.equals(phoneDigits, digits(lawyer.getLawyerPhone())) 
                    && Objects.equals(idnumDigits, digits(lawyer.getLawyerIdnum()))) {
                    lawOpt = Optional.of(lawyer);
                    break;
                }
            }
        }
        
        if (lawOpt.isPresent()) {
            return lawOpt.get().getLawyerId();
        }

        return "NOT_FOUND";
    }

    // 비밀번호 재설정 아이디→member/lawyer 판별 후 검증
    @Transactional
    public String resetPassword(String memberId,
                                String memberPhone,
                                String memberIdnum,
                                String newPassword,
                                String confirmPassword) {
        if (!Objects.equals(newPassword, confirmPassword)) return "MISMATCH";

        String phone = digits(memberPhone);
        String idnum = digits(memberIdnum);

        Optional<MemberEntity> mOpt = memberRepository.findByMemberId(memberId);
        if (mOpt.isPresent()) {
            MemberEntity me = mOpt.get();
            boolean verified = Objects.equals(phone, digits(me.getMemberPhone()))
                             && Objects.equals(idnum, digits(me.getMemberIdnum()));
            if (!verified) return "FAIL";
            me.setMemberPass(passwordEncoder.encode(newPassword));
            memberRepository.save(me);
            return "OK";
        }

        Optional<LawyerEntity> lOpt = lawyerRepository.findByLawyerId(memberId);
        if (lOpt.isPresent()) {
            LawyerEntity le = lOpt.get();
            boolean verified = Objects.equals(phone, digits(le.getLawyerPhone()))
                             && Objects.equals(idnum, digits(le.getLawyerIdnum()));
            if (!verified) return "FAIL";
            le.setLawyerPass(passwordEncoder.encode(newPassword));
            lawyerRepository.save(le);
            return "OK";
        }

        return "FAIL";
    }

    public record MemberUpdateResult(String newUserId, MemberEntity member) {}

    // OAuth2 일반 회원가입
    @Transactional
    public MemberEntity joinOAuthMember(TemporaryOauthDTO temp, MemberDTO joinMember) {
        MemberEntity memberEntity = convertMemberEntity(joinMember);
        memberEntity.setMemberId(temp.getProvider() + temp.getProviderId());
        memberEntity.setMemberPass(temp.getProviderId() + temp.getEmail());
        memberEntity.setMemberName(temp.getName());
        memberEntity.setMemberEmail(temp.getEmail());
        memberEntity.setMemberActive(1);
        memberEntity.setMemberPoint(0);
        memberEntity.setMemberProvider(temp.getProvider());
        memberEntity.setMemberProviderId(temp.getProviderId());
        
        // 수신동의: "1" 또는 "0"으로 저장 (동의하면 "1", 아니면 "0")
        String agreeValue = (joinMember.getMemberAgree() != null && "1".equals(joinMember.getMemberAgree())) ? "1" : "0";
        memberEntity.setMemberAgree(agreeValue);

        return memberRepository.save(memberEntity);
    }

    // 문의 상세 조회 필요한 id 와 name
    public MemberDTO qMemberInquiry(Integer memberIdx){
        MemberEntity memberEntity = memberRepository.findById(memberIdx).orElse(null);
        MemberDTO memberDTO = convertMemberDTO(memberEntity);
        return memberDTO;
    }

    // gmodify에서 관심분야 3개를 모두 채운 DTO 반환
    private MemberDTO convertMemberDTOFull(MemberEntity e) {
        MemberDTO d = new MemberDTO();
        d.setMemberIdx(e.getMemberIdx());
        d.setMemberId(e.getMemberId());
        d.setMemberPass(e.getMemberPass());
        d.setMemberName(e.getMemberName());
        d.setMemberIdnum(e.getMemberIdnum());
        d.setMemberEmail(e.getMemberEmail());
        d.setMemberPhone(e.getMemberPhone());
        d.setMemberAgree(e.getMemberAgree());
        d.setMemberNickname(e.getMemberNickname());
        d.setMemberActive(e.getMemberActive());
        d.setInterestIdx1(e.getInterestIdx1());
        d.setInterestIdx2(e.getInterestIdx2());
        d.setInterestIdx3(e.getInterestIdx3());
        return d;
    }

    @Transactional(readOnly = true)
    public MemberDTO loadProfileForModify() {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"MEMBER".equalsIgnoreCase(login.getRole())) {
            throw new IllegalStateException("일반회원만 접근 가능합니다.");
        }
        MemberEntity me = memberRepository.findById(login.getMemberIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));
        return convertMemberDTOFull(me);
    }

    @Transactional
    public MemberUpdateResult updateProfileForCurrent(MemberDTO dto, String newPassword, String confirmPassword) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"MEMBER".equalsIgnoreCase(login.getRole())) {
            throw new IllegalStateException("일반회원만 접근 가능합니다.");
        }
        return updateMemberProfile(dto, newPassword, confirmPassword, null, login.getMemberIdx());
    }

    // 관리자가 다른 회원 정보를 수정하는 경우
    @Transactional
    public MemberUpdateResult updateProfileForMemberByIdx(Integer memberIdx, MemberDTO dto, String newPassword, String confirmPassword) {
        if (memberIdx == null) {
            throw new IllegalArgumentException("회원 번호가 필요합니다.");
        }
        return updateMemberProfile(dto, newPassword, confirmPassword, null, memberIdx);
    }

    @Transactional
    public String changePasswordWithVerificationForCurrent(String memberPhone, String memberIdnum,
                                                           String newPassword, String confirmPassword) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"MEMBER".equalsIgnoreCase(login.getRole())) {
            return "FAIL";
        }
        return resetPassword(login.getUserId(), memberPhone, memberIdnum, newPassword, confirmPassword);
    }

    @Transactional
    public boolean deactivateWithVerificationForCurrent(String memberPhone, String memberIdnum) {
        UserMasterDTO login = currentLoginUserOrThrow();
        if (login.getRole() == null || !"MEMBER".equalsIgnoreCase(login.getRole())) {
            return false;
        }
        String phone = digits(memberPhone);
        String idnum = digits(memberIdnum);

        MemberEntity me = memberRepository.findById(login.getMemberIdx())
                .orElseThrow(() -> new IllegalStateException("회원 정보를 찾을 수 없습니다."));

        boolean verified = Objects.equals(phone, digits(me.getMemberPhone()))
                        && Objects.equals(idnum, digits(me.getMemberIdnum()));
        if (!verified) return false;

        me.setMemberActive(0);
        memberRepository.save(me);
        return true;
    }

    // ===== 내가 쓴 글  =====

    @Transactional(readOnly = true)
    public List<BoardDTO> getMyBoards(Integer memberIdx) {
        if (memberIdx == null) return java.util.Collections.emptyList();

        return boardRepository
                .findTop5ByMemberMemberIdxAndBoardActiveOrderByBoardRegDateDesc(memberIdx, 1)
                .stream()
                .map(this::toBoardDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getMyComments(Integer memberIdx) {
        if (memberIdx == null) return java.util.Collections.emptyList();

        return commentRepository
                .findTop5ByMemberIdxAndCommentActiveOrderByCommentRegDateDesc(memberIdx, 1)
                .stream()
                .map(this::toCommentDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getMyCommentsByLawyer(Integer lawyerIdx) {
        if (lawyerIdx == null) return java.util.Collections.emptyList();

        return commentRepository
                .findTop5ByLawyerIdxAndCommentActiveOrderByCommentRegDateDesc(lawyerIdx, 1)
                .stream()
                .map(this::toCommentDTO)
                .toList();
    }

    private BoardDTO toBoardDTO(BoardEntity e) {
        BoardDTO d = new BoardDTO();
        d.setBoardIdx(e.getBoardIdx());
        d.setBoardTitle(e.getBoardTitle());
        d.setBoardRegDate(e.getBoardRegDate());
        d.setBoardViews(e.getBoardViews());
        return d;
    }

    private CommentDTO toCommentDTO(CommentEntity e) {
        CommentDTO d = new CommentDTO();
        d.setCommentIdx(e.getCommentIdx());
        d.setCommentContent(e.getCommentContent());
        d.setCommentRegDate(e.getCommentRegDate());
        d.setNewsIdx(e.getNewsIdx());
        d.setMemberIdx(e.getMemberIdx());
        d.setLawyerIdx(e.getLawyerIdx());
        d.setCommentActive(e.getCommentActive());
        return d;
    }

    // 댓글을 남긴 newsboard 게시글 목록 조회
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.soldesk.team_project.entity.NewsBoardEntity> getMyCommentedNewsBoards(
            Integer memberIdx, org.springframework.data.domain.Pageable pageable) {
        if (memberIdx == null) {
            return org.springframework.data.domain.Page.empty(pageable);
        }

        // 댓글을 남긴 newsIdx 목록 조회 (중복 제거, 최신순)
        List<Integer> newsIdxList = commentRepository
                .findDistinctNewsIdxByMemberIdxAndCommentActiveOrderByCommentRegDateDesc(memberIdx);

        if (newsIdxList.isEmpty()) {
            return org.springframework.data.domain.Page.empty(pageable);
        }

        // newsIdx 목록으로 NewsBoardEntity 조회 (newsActive=1인 것만)
        List<com.soldesk.team_project.entity.NewsBoardEntity> allNewsBoards = 
                newsBoardRepository.findAllById(newsIdxList)
                    .stream()
                    .filter(n -> n.getNewsActive() == 1) // newsActive=1인 것만 필터링
                    .collect(Collectors.toList());

        // 최신 댓글 순으로 정렬 (newsIdxList 순서 유지)
        // Map을 사용하여 빠른 조회
        java.util.Map<Integer, com.soldesk.team_project.entity.NewsBoardEntity> newsBoardMap = 
                allNewsBoards.stream()
                    .collect(Collectors.toMap(
                        com.soldesk.team_project.entity.NewsBoardEntity::getNewsIdx,
                        e -> e,
                        (e1, e2) -> e1
                    ));

        // newsIdxList 순서대로 정렬된 리스트 생성 (newsActive=1인 것만)
        List<com.soldesk.team_project.entity.NewsBoardEntity> newsBoards = newsIdxList.stream()
                .map(newsBoardMap::get)
                .filter(Objects::nonNull)
                .filter(n -> n.getNewsActive() == 1) // 추가 필터링 (안전장치)
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), newsBoards.size());
        List<com.soldesk.team_project.entity.NewsBoardEntity> pagedList = 
                start < newsBoards.size() ? newsBoards.subList(start, end) : new ArrayList<>();

        return new org.springframework.data.domain.PageImpl<>(
                pagedList, 
                pageable, 
                newsBoards.size()
        );
    }

    // 변호사가 댓글을 남긴 newsboard 게시글 목록 조회
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.soldesk.team_project.entity.NewsBoardEntity> getMyCommentedNewsBoardsByLawyer(
            Integer lawyerIdx, org.springframework.data.domain.Pageable pageable) {
        if (lawyerIdx == null) {
            return org.springframework.data.domain.Page.empty(pageable);
        }

        // 댓글을 남긴 newsIdx 목록 조회 (중복 제거, 최신순)
        List<Integer> newsIdxList = commentRepository
                .findDistinctNewsIdxByLawyerIdxAndCommentActiveOrderByCommentRegDateDesc(lawyerIdx);

        if (newsIdxList.isEmpty()) {
            return org.springframework.data.domain.Page.empty(pageable);
        }

        // newsIdx 목록으로 NewsBoardEntity 조회 (newsActive=1인 것만)
        List<com.soldesk.team_project.entity.NewsBoardEntity> allNewsBoards = 
                newsBoardRepository.findAllById(newsIdxList)
                    .stream()
                    .filter(n -> n.getNewsActive() == 1) // newsActive=1인 것만 필터링
                    .collect(Collectors.toList());

        // 최신 댓글 순으로 정렬 (newsIdxList 순서 유지)
        // Map을 사용하여 빠른 조회
        java.util.Map<Integer, com.soldesk.team_project.entity.NewsBoardEntity> newsBoardMap = 
                allNewsBoards.stream()
                    .collect(Collectors.toMap(
                        com.soldesk.team_project.entity.NewsBoardEntity::getNewsIdx,
                        e -> e,
                        (e1, e2) -> e1
                    ));

        // newsIdxList 순서대로 정렬된 리스트 생성 (newsActive=1인 것만)
        List<com.soldesk.team_project.entity.NewsBoardEntity> newsBoards = newsIdxList.stream()
                .map(newsBoardMap::get)
                .filter(Objects::nonNull)
                .filter(n -> n.getNewsActive() == 1) // 추가 필터링 (안전장치)
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), newsBoards.size());
        List<com.soldesk.team_project.entity.NewsBoardEntity> pagedList = 
                start < newsBoards.size() ? newsBoards.subList(start, end) : new ArrayList<>();

        return new org.springframework.data.domain.PageImpl<>(
                pagedList, 
                pageable, 
                newsBoards.size()
        );
    }
}
