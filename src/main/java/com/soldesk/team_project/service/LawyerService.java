package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LawyerService {
    
    private final LawyerRepository lawyerRepository;

    private LawyerDTO convertLawyerDTO (LawyerEntity lawyerEntity) {
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
            case "idnum": lawyerEntityList = lawyerRepository.findByLawyerIdnumContainingOrderByMemberIdnumAsc(keyword); break;
            case "email": lawyerEntityList = lawyerRepository.findByLawyerEmailContainingIgnoreCaseOrderByLawyerEmailAsc(keyword); break;
            case "phone": lawyerEntityList = lawyerRepository.findByLawyerPhoneContainingOrderByLawyerPhoneAsc(keyword); break;
            case "nickname": lawyerEntityList = lawyerRepository.findByLawyerNicknameContainingIgnoreCaseOrderByLawyerNicknameAsc(keyword); break;
            case "auth": lawyerEntityList = lawyerRepository.findByLawyerAuthContainingOrderByLawyerAuthAsc(Integer.valueOf(keyword)); break;
            case "address": lawyerEntityList = lawyerRepository.findByLawyerAddressContainingIgnoreCaseOrderByLawyerAddressAsc(keyword); break;
            case "tel": lawyerEntityList = lawyerRepository.findByLawyerTelContainingOrderByLawyerTelAsc(keyword); break;
            case "comment": lawyerEntityList = lawyerRepository.findByLawyerCommentContainingIgnoreCaseOrderByLawyerCommentAsc(keyword); break;
            
            default: lawyerEntityList = lawyerRepository.findAll(); break;
        }
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

}
