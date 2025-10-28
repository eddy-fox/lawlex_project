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
        lawyerDTO.setLawyerActive(lawyerEntity.getLawyerActive());
        lawyerDTO.setInterestIdx(lawyerEntity.getInterestIdx());

        return lawyerDTO;
    }

    // 전체 변호사 조회
    public List<LawyerDTO> getAllLawyer() {
        List<LawyerEntity> lawyerEntityList = lawyerRepository.findByLawyerActive(1);
        
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 변호사 검색
    public List<LawyerDTO> searchLawyers(String searchType, String keyword) {
        List<LawyerEntity> lawyerEntityList;

        switch (searchType) {
            case "idx": lawyerEntityList = lawyerRepository.
                findByLawyerIdxAndLawyerActive(Integer.valueOf(keyword), 1); break;
            case "id": lawyerEntityList = lawyerRepository.
                findByLawyerIdContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1); break;
            case "name": lawyerEntityList = lawyerRepository.
                findByLawyerNameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1); break;
            case "idnum": lawyerEntityList = lawyerRepository.
                findByLawyerIdnumContainingAndLawyerActiveOrderByLawyerIdnumAsc(keyword, 1); break;
            case "email": lawyerEntityList = lawyerRepository.
                findByLawyerEmailContainingIgnoreCaseAndLawyerActiveOrderByLawyerEmailAsc(keyword, 1); break;
            case "phone": lawyerEntityList = lawyerRepository.
                findByLawyerPhoneContainingAndLawyerActiveOrderByLawyerPhoneAsc(keyword, 1); break;
            case "nickname": lawyerEntityList = lawyerRepository.
                findByLawyerNicknameContainingIgnoreCaseAndLawyerActiveOrderByLawyerNicknameAsc(keyword, 1); break;
            case "auth": lawyerEntityList = lawyerRepository.
                findByLawyerAuthAndLawyerActiveOrderByLawyerAuthAsc(Integer.valueOf(keyword), 1); break;
            case "address": lawyerEntityList = lawyerRepository.
                findByLawyerAddressContainingIgnoreCaseAndLawyerActiveOrderByLawyerAddressAsc(keyword, 1); break;
            case "tel": lawyerEntityList = lawyerRepository.
                findByLawyerTelContainingAndLawyerActiveOrderByLawyerTelAsc(keyword, 1); break;
            case "comment": lawyerEntityList = lawyerRepository.
                findByLawyerCommentContainingIgnoreCaseAndLawyerActiveOrderByLawyerCommentAsc(keyword, 1); break;
            
            default: lawyerEntityList = lawyerRepository.findByLawyerActive(1); break;
        }
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

    // 광고 게재
    // public AdDTO 

}
