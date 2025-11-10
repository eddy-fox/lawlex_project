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
// import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.MemberEntity;
// import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    // private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEncoder encoder;

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
        memberDTO.setInterestName(memberEntity.getInterest().getInterestName());

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
        public MemberEntity login(String memberId, String memberPass){
        MemberEntity m = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다."));
        if (Objects.equals(m.getMemberActive(), 0))
            throw new IllegalArgumentException("탈퇴한 계정입니다.");
        if (!encoder.matches(memberPass, m.getMemberPass()))
            throw new IllegalArgumentException("아이디 또는 비밀번호가 올바르지 않습니다.");
        return m;
    }

    public MemberEntity findByPhoneAndIdnum(String phone, String idnum){
        return memberRepository.findByMemberPhoneAndMemberIdnum(phone, idnum).orElse(null);
    }

    @Transactional
    public boolean resetPassword(String memberId, String phone, String idnum, String newPw){
        MemberEntity m = memberRepository
                .findByMemberIdAndMemberPhoneAndMemberIdnum(memberId, phone, idnum)
                .orElse(null);
        if (m == null) return false;
        m.changePassword(encoder.encode(newPw));
        return true;
    }

    public boolean verifyIdxPhoneBirth(Integer memberIdx, String phone, String idnum){
        return memberRepository
                .findByMemberIdxAndMemberPhoneAndMemberIdnum(memberIdx, phone, idnum)
                .isPresent();
    }

    /**
     * 마이페이지 - 회원정보 수정
     * - 비밀번호: 값이 있으면 BCrypt 인코딩 후 변경
     * - 닉네임: 값이 있으면 변경
     * - 관심분야: interestIdx1/2/3 저장 + (레거시) interest_idx는 1번 값으로 동기화(존재 시)
     */
    @Transactional
    public void updateProfile(MemberDTO dto){
        MemberEntity m = memberRepository.findById(dto.getMemberIdx())
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));

        // 비밀번호 변경 (입력 시에만)
        if (dto.getMemberPass() != null && !dto.getMemberPass().isBlank()){
            m.changePassword(encoder.encode(dto.getMemberPass().trim()));
        }

        // 닉네임 변경 (입력 시에만)
        if (dto.getMemberNickname() != null && !dto.getMemberNickname().isBlank()){
            m.changeNickname(dto.getMemberNickname().trim());
        }

        // 관심분야 저장 (null 허용)
        Integer i1 = dto.getInterestIdx1();
        Integer i2 = dto.getInterestIdx2();
        Integer i3 = dto.getInterestIdx3();

        if (i1 != null) m.setInterestIdx1(i1);
        if (i2 != null) m.setInterestIdx2(i2);
        if (i3 != null) m.setInterestIdx3(i3);

        // 레거시 컬럼 동기화: 첫 번째 선택값이 있으면 그 값으로
        if (i1 != null) {
            m.setInterestIdx(i1);
        }
    }

    // ✅ 회원 탈퇴(비활성화)
    @Transactional
    public boolean deactivate(Integer memberIdx, String phone, String idnum){
        MemberEntity m = memberRepository.findById(memberIdx).orElse(null);
        if (m == null) return false;

        // 서버에서도 전화/생년월일 재검증
        if (!Objects.equals(m.getMemberPhone(), phone)) return false;
        if (!Objects.equals(m.getMemberIdnum(), idnum)) return false;

        m.deactivate(); // member_active = 0
        return true;
    }
}
