package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.MemberDTO;
import com.soldesk.team_project.dto.TemporaryOauthDTO;
import com.soldesk.team_project.entity.InterestEntity;
// import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.MemberInterestEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.UserMasterRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.soldesk.team_project.entity.UserMasterEntity;
import com.soldesk.team_project.entity.LawyerEntity;



@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;

    private final UserMasterRepository userMasterRepository;
    private final LawyerRepository lawyerRepository;


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
        memberDTO.setInterestIdx1(memberEntity.getInterestIdx());
        
        return memberDTO;
    }

    // private MemberEntity convertMemberEntity (MemberDTO memberDTO) {
    //     MemberEntity memberEntity = new MemberEntity();
    //     memberEntity.setMemberIdx(memberDTO.getMemberIdx());
    //     memberEntity.setMemberId(memberDTO.getMemberId());
    //     memberEntity.setMemberPass(memberDTO.getMemberPass());
    //     memberEntity.setMemberName(memberDTO.getMemberName());
    //     memberEntity.setMemberIdnum(memberDTO.getMemberIdnum());
    //     memberEntity.setMemberEmail(memberDTO.getMemberEmail());
    //     memberEntity.setMemberPhone(memberDTO.getMemberPhone());
    //     memberEntity.setMemberAgree(memberDTO.getMemberAgree());
    //     memberEntity.setMemberNickname(memberDTO.getMemberNickname());
    //     InterestEntity interestEntity = interestRepository.findById(memberDTO.getInterestIdx()).orElse(null);
    //     memberEntity.setMemberInterest(interestEntity);
        
    //     return memberEntity;
    // }

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
                } break;
            case "id": memberEntityList = memberRepository
                .findByMemberIdContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(keyword, 1); break;
            case "name": memberEntityList = memberRepository
                .findByMemberNameContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(keyword, 1); break;
            case "idnum": memberEntityList = memberRepository
                .findByMemberIdnumContainingAndMemberActiveOrderByMemberIdnumAsc(keyword, 1); break;
            case "email": memberEntityList = memberRepository
                .findByMemberEmailContainingIgnoreCaseAndMemberActiveOrderByMemberEmailAsc(keyword, 1); break;
            case "phone": memberEntityList = memberRepository
                .findByMemberPhoneContainingAndMemberActiveOrderByMemberPhoneAsc(keyword, 1); break;
            case "nickname": memberEntityList = memberRepository
                .findByMemberNicknameContainingIgnoreCaseAndMemberActiveOrderByMemberNicknameAsc(keyword, 1); break;
            default: memberEntityList = memberRepository.findByMemberActive(1);
         break;
        }
        return memberEntityList.stream()
            .map(memberEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }

    //특정 회원 검색
    public MemberEntity getMember(String memberName) {

        Optional<MemberEntity> member = this.memberRepository.findByMemberName(memberName);
        if(member.isPresent()) {
            return member.get();
        } else {
            throw new DataNotFoundException("member not found");
        }
        
    }
       // ====== 공통 유틸 ======
    private static String digits(String s){ return s == null ? null : s.replaceAll("\\D",""); }
    private static boolean notBlank(String s){ return s != null && !s.isBlank(); }
    private static String roleUpper(String s){ return s == null ? "" : s.toUpperCase(); }

    // ====== 공통: 아이디 중복 체크 ======
    public boolean isUserIdDuplicate(String userId){
        return userMasterRepository.existsByUserId(userId);
    }

    // ====== 일반 회원가입 ======
    @Transactional
    public void joinNormal(MemberDTO dto){
        if (isUserIdDuplicate(dto.getMemberId())){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
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
                .interestIdx1(dto.getInterestIdx1())
                .interestIdx2(dto.getInterestIdx2())
                .interestIdx3(dto.getInterestIdx3())
                .build();
        me = memberRepository.save(me);

        UserMasterEntity u = UserMasterEntity.builder()
                .userId(dto.getMemberId())
                .password(enc)
                .status("ACTIVE")
                .memberIdx(me.getMemberIdx())
                .role("MEMBER")
                .build();
        userMasterRepository.save(u);
    }

    // ====== 일반회원 프로필 수정 (아이디/비번/닉네임/이메일/관심3개) ======
    @Transactional
    public MemberUpdateResult updateMemberProfile(MemberDTO dto,
                                                  String newPassword,
                                                  String confirmPassword,
                                                  Long userIdx,
                                                  Integer memberIdx){
        UserMasterEntity u = userMasterRepository.findById(userIdx).orElseThrow();
        MemberEntity me = memberRepository.findById(memberIdx).orElseThrow();

        // 아이디 변경
        if (notBlank(dto.getMemberId()) && !dto.getMemberId().equals(u.getUserId())){
            if (isUserIdDuplicate(dto.getMemberId())){
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }
            u.setUserId(dto.getMemberId());
            me.setMemberId(dto.getMemberId());
        }

        // 비밀번호 변경
        if (notBlank(newPassword) || notBlank(confirmPassword)){
            if (!notBlank(newPassword) || !newPassword.equals(confirmPassword)){
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            String enc = passwordEncoder.encode(newPassword);
            u.setPassword(enc);
            me.setMemberPass(enc);
        }

        // 닉네임/이메일/관심3
        me.setMemberNickname(dto.getMemberNickname());
        me.setMemberEmail(dto.getMemberEmail());
        me.setInterestIdx1(dto.getInterestIdx1());
        me.setInterestIdx2(dto.getInterestIdx2());
        me.setInterestIdx3(dto.getInterestIdx3());

        userMasterRepository.save(u);
        memberRepository.save(me);

        return new MemberUpdateResult(u.getUserId(), me);
    }
    
    //관심분야 중복 선택 방지
    private static Integer nz(Integer v){ return v == null ? -1 : v; }
    private void validateDistinctInterests(MemberDTO d){
    Integer a = nz(d.getInterestIdx1()), b = nz(d.getInterestIdx2()), c = nz(d.getInterestIdx3());
    if (a.equals(b) || a.equals(c) || b.equals(c)) {
        throw new IllegalArgumentException("관심 분야는 서로 다른 항목으로 선택해주세요.");
    }
}

    public MemberEntity saveProcess(MemberDTO memberDTO, TemporaryOauthDTO tempUser) {
    MemberEntity memberEntity = MemberEntity.builder()
        .memberId(tempUser.getEmail())
        .memberPass("{noop}oauth2")
        .memberName(tempUser.getName())
        .memberEmail(tempUser.getEmail())
        .memberPhone(memberDTO.getMemberPhone())
        .memberIdnum(memberDTO.getMemberIdnum())
        .interestIdx1(memberDTO.getInterestIdx())
        .memberActive(1)
        .provider(tempUser.getProvider())
        .build();
        return memberRepository.save(memberEntity);
    }


    // ====== 아이디 찾기 ======
    public String findId(String memberPhone, String memberIdnum){
        String phone = digits(memberPhone);
        String idnum = digits(memberIdnum);

        var mem = memberRepository.findByMemberPhoneAndMemberIdnum(phone, idnum)
                .flatMap(m -> userMasterRepository.findByMemberIdx(m.getMemberIdx()));
        if (mem.isPresent()) return mem.get().getUserIdx() + "/" + mem.get().getUserId();

        var law = lawyerRepository.findByLawyerPhoneAndLawyerIdnum(phone, idnum)
                .flatMap(l -> userMasterRepository.findByLawyerIdx(l.getLawyerIdx()));
        return law.map(u -> u.getUserIdx() + "/" + u.getUserId()).orElse("NOT_FOUND");
    }

    // ====== 비밀번호 재설정 ======
    @Transactional
    public String resetPassword(String memberId,
                                String memberPhone,
                                String memberIdnum,
                                String newPassword,
                                String confirmPassword){
        if (!newPassword.equals(confirmPassword)) return "MISMATCH";

        String phone = digits(memberPhone);
        String idnum = digits(memberIdnum);

        Optional<UserMasterEntity> uOpt = userMasterRepository.findByUserId(memberId);
        if (uOpt.isEmpty()) return "FAIL";
        UserMasterEntity u = uOpt.get();

        boolean verified = switch (roleUpper(u.getRole())){
            case "MEMBER" -> {
                MemberEntity me = (u.getMemberIdx()!=null) ? memberRepository.findById(u.getMemberIdx()).orElse(null) : null;
                yield me != null && phone.equals(me.getMemberPhone()) && idnum.equals(me.getMemberIdnum());
            }
            case "LAWYER" -> {
                LawyerEntity le = (u.getLawyerIdx()!=null) ? lawyerRepository.findById(u.getLawyerIdx()).orElse(null) : null;
                yield le != null && phone.equals(le.getLawyerPhone()) && idnum.equals(le.getLawyerIdnum());
            }
            case "ADMIN" -> {
                // 관리자 인증 조건은 필요 시 확장
                yield false;
            }
            default -> false;
        };
        if (!verified) return "FAIL";

        String enc = passwordEncoder.encode(newPassword);
        u.setPassword(enc);

        // 레거시 동기화
        if ("MEMBER".equalsIgnoreCase(u.getRole()) && u.getMemberIdx()!=null){
            memberRepository.findById(u.getMemberIdx()).ifPresent(m -> m.setMemberPass(enc));
        }
        if ("LAWYER".equalsIgnoreCase(u.getRole()) && u.getLawyerIdx()!=null){
            lawyerRepository.findById(u.getLawyerIdx()).ifPresent(l -> l.setLawyerPass(enc));
        }
        userMasterRepository.save(u);
        return "OK";
    }

    // ====== 결과 객체 ======
    public record MemberUpdateResult(String newUserId, MemberEntity member) {}

    
}
