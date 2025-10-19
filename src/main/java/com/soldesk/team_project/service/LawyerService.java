package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

<<<<<<< HEAD
=======
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.LawyerRepository;

>>>>>>> f777e53d1d673ed96dc12f504f83a1aca9569091
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
    
<<<<<<< HEAD
    private final LawyerRepository LawyerRepository;

    private LawyerDTO convertLawyerDTO (LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new MemberDTO();
=======
    private final LawyerRepository lawyerRepository;

    private LawyerDTO convertLawyerDTO (LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new LawyerDTO();
>>>>>>> f777e53d1d673ed96dc12f504f83a1aca9569091
        lawyerDTO.setLawyerIdx(lawyerEntity.getLawyerIdx());
        lawyerDTO.setLawyerId(lawyerEntity.getLawyerId());
        lawyerDTO.setLawyerPass(lawyerEntity.getLawyerPass());
        lawyerDTO.setLawyerName(lawyerEntity.getLawyerName());
        lawyerDTO.setLawyerIdnum(lawyerEntity.getLawyerIdnum());
        lawyerDTO.setLawyerEmail(lawyerEntity.getLawyerEmail());
        lawyerDTO.setLawyerPhone(lawyerEntity.getLawyerPhone());
        lawyerDTO.setLawyerAgree(lawyerEntity.getLawyerAgree());
<<<<<<< HEAD
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
=======
        lawyerDTO.setLawyerNickname(lawyerEntity.getLawyerNickname());
        lawyerDTO.setLawyerAuth(lawyerEntity.getLawyerAuth());
        lawyerDTO.setLawyerAddress(lawyerEntity.getLawyerAddress());
        lawyerDTO.setLawyerTel(lawyerEntity.getLawyerTel());
        lawyerDTO.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
        lawyerDTO.setLawyerComment(lawyerEntity.getLawyerComment());
        lawyerDTO.setLawyerLike(lawyerEntity.getLawyerLike());
        lawyerDTO.setLawyerAnswerCnt(lawyerEntity.getLawyerAnswerCnt());
        lawyerDTO.setInterestName(lawyerEntity.getInterest().getInterestName());

        return lawyerDTO;
    }

    // 전체 변호사 조회
    public List<LawyerDTO> getAllLawyer() {
        List<LawyerEntity> lawyerEntityList = lawyerRepository.findAll();
        
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 변호사 검색
    public List<LawyerDTO> searchLawyers(String searchType, String keyword) {
        List<LawyerEntity> lawyerEntityList;

        switch (searchType) {
            case "idx": lawyerEntityList = lawyerRepository.findByLawyerIdx(Integer.valueOf(keyword)); break;
            case "id": lawyerEntityList = lawyerRepository.findByLawyerIdContainingIgnoreCaseOrderByLawyerIdAsc(keyword); break;
            case "name": lawyerEntityList = lawyerRepository.findByLawyerNameContainingIgnoreCaseOrderByLawyerIdAsc(keyword); break;
            case "idnum": lawyerEntityList = lawyerRepository.findByLawyerIdnumContainingOrderByLawyerIdnumAsc(keyword); break;
            case "email": lawyerEntityList = lawyerRepository.findByLawyerEmailContainingIgnoreCaseOrderByLawyerEmailAsc(keyword); break;
            case "phone": lawyerEntityList = lawyerRepository.findByLawyerPhoneContainingOrderByLawyerPhoneAsc(keyword); break;
            case "nickname": lawyerEntityList = lawyerRepository.findByLawyerNicknameContainingIgnoreCaseOrderByLawyerNicknameAsc(keyword); break;
            case "auth": lawyerEntityList = lawyerRepository.findByLawyerAuthOrderByLawyerAuthAsc(Integer.valueOf(keyword)); break;
            case "address": lawyerEntityList = lawyerRepository.findByLawyerAddressContainingIgnoreCaseOrderByLawyerAddressAsc(keyword); break;
            case "tel": lawyerEntityList = lawyerRepository.findByLawyerTelContainingOrderByLawyerTelAsc(keyword); break;
            case "comment": lawyerEntityList = lawyerRepository.findByLawyerCommentContainingIgnoreCaseOrderByLawyerCommentAsc(keyword); break;
            
            default: lawyerEntityList = lawyerRepository.findAll(); break;
        }
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
>>>>>>> f777e53d1d673ed96dc12f504f83a1aca9569091
    }

}
