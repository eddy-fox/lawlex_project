package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
    
    private final LawyerRepository LawyerRepository;

    private LawyerDTO convertLawyerDTO (LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new MemberDTO();
        lawyerDTO.setLawyerIdx(lawyerEntity.getLawyerIdx());
        lawyerDTO.setLawyerId(lawyerEntity.getLawyerId());
        lawyerDTO.setLawyerPass(lawyerEntity.getLawyerPass());
        lawyerDTO.setLawyerName(lawyerEntity.getLawyerName());
        lawyerDTO.setLawyerIdnum(lawyerEntity.getLawyerIdnum());
        lawyerDTO.setLawyerEmail(lawyerEntity.getLawyerEmail());
        lawyerDTO.setLawyerPhone(lawyerEntity.getLawyerPhone());
        lawyerDTO.setLawyerAgree(lawyerEntity.getLawyerAgree());
        lawyerDTO.setLawyerNickname(lawyerEntity.getMemberNickname());
        lawyerDTO.setInterestName(lawyerEntity.getInterest().getInterestName());

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

        if (searchType.equals("all")) {
            memberEntityList = memberRepository.findAll();
        } else {
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
        }
        return memberEntityList.stream()
            .map(memberEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }

}
