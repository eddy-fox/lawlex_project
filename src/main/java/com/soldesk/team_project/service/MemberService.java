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
        memberDTO.setInterestIdx1(memberEntity.getInterestIdx()); // 기존 로직 유지
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
    public MemberDTO searchSessionMember(int memberIdx) {
        MemberEntity memberEntity = memberRepository.findById(memberIdx).orElse(null);
        MemberDTO memberDTO = convertMemberDTO(memberEntity);
        return memberDTO;
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

        MemberEntity me = MemberEntity.builder()
                .memberId(dto.getMemberId())
                .memberPass(enc)
                .memberName(dto.getMemberName())
                .memberEmail(dto.getMemberEmail())
                .memberPhone(digits(dto.getMemberPhone()))
                .memberIdnum(digits(dto.getMemberIdnum()))
                .memberNickname(dto.getMemberNickname())
                .memberAgree(dto.getMemberAgree())
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

    // OAuth2
    // @Transactional
    // public MemberEntity saveProcess(MemberDTO memberDTO, TemporaryOauthDTO tempUser) {
    //     MemberEntity memberEntity = MemberEntity.builder()
    //             .memberId(tempUser.getEmail())
    //             .memberPass("{noop}oauth2")
    //             .memberName(tempUser.getName())
    //             .memberEmail(tempUser.getEmail())
    //             .memberPhone(digits(memberDTO.getMemberPhone()))
    //             .memberIdnum(digits(memberDTO.getMemberIdnum()))
    //             .interestIdx1(memberDTO.getInterestIdx1())
    //             .memberActive(1)
    //             .provider(tempUser.getProvider())
    //             .build();
    //     return memberRepository.save(memberEntity);
    // }

    // 아이디 찾기 member → lawyer 순서
    public String findId(String memberPhone, String memberIdnum) {
        String phone = digits(memberPhone);
        String idnum = digits(memberIdnum);

        var memOpt = memberRepository.findByMemberPhoneAndMemberIdnum(phone, idnum);
        if (memOpt.isPresent()) {
            return memOpt.get().getMemberId();
        }

        var lawOpt = lawyerRepository.findByLawyerPhoneAndLawyerIdnum(phone, idnum);
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
            boolean verified = Objects.equals(phone, me.getMemberPhone())
                             && Objects.equals(idnum, me.getMemberIdnum());
            if (!verified) return "FAIL";
            me.setMemberPass(passwordEncoder.encode(newPassword));
            memberRepository.save(me);
            return "OK";
        }

        Optional<LawyerEntity> lOpt = lawyerRepository.findByLawyerId(memberId);
        if (lOpt.isPresent()) {
            LawyerEntity le = lOpt.get();
            boolean verified = Objects.equals(phone, le.getLawyerPhone())
                             && Objects.equals(idnum, le.getLawyerIdnum());
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

        boolean verified = Objects.equals(phone, me.getMemberPhone())
                        && Objects.equals(idnum, me.getMemberIdnum());
        if (!verified) return false;

        me.setMemberActive(0);
        memberRepository.save(me);
        return true;
    }

    // ===== 내가 쓴 글 / 댓글 =====

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
}
