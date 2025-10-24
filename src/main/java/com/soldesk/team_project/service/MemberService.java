package com.soldesk.team_project.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.MemberDTO;
// import com.soldesk.team_project.entity.InterestEntity;
import com.soldesk.team_project.entity.MemberEntity;
// import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    // private final InterestRepository interestRepository;

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
        memberDTO.setInterestName(memberEntity.getInterest().getInterestName());
        this.memberRepository.save(memberEntity);

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
        List<MemberEntity> memberEntityList = memberRepository.findAll();
        
        return memberEntityList.stream()
            .map(memberEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 회원 검색
    public List<MemberDTO> searchMembers(String searchType, String keyword) {
        List<MemberEntity> memberEntityList;

        switch (searchType) {
            case "idx": memberEntityList = memberRepository.findByMemberIdx(Integer.valueOf(keyword)); break;
            case "id": memberEntityList = memberRepository.findByMemberIdContainingIgnoreCaseOrderByMemberIdAsc(keyword); break;
            case "name": memberEntityList = memberRepository.findByMemberNameContainingIgnoreCaseOrderByMemberIdAsc(keyword); break;
            case "idnum": memberEntityList = memberRepository.findByMemberIdnumContainingOrderByMemberIdnumAsc(keyword); break;
            case "email": memberEntityList = memberRepository.findByMemberEmailContainingIgnoreCaseOrderByMemberEmailAsc(keyword); break;
            case "phone": memberEntityList = memberRepository.findByMemberPhoneContainingOrderByMemberPhoneAsc(keyword); break;
            case "nickname": memberEntityList = memberRepository.findByMemberNicknameContainingIgnoreCaseOrderByMemberNicknameAsc(keyword); break;
            default: memberEntityList = memberRepository.findAll(); break;
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
}
