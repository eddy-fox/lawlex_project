package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.RankingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RankingRepository rankingRepository;
    
    private LawyerDTO convertLawyerDTO(LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerIdx(lawyerEntity.getLawyerIdx());
        lawyerDTO.setLawyerName(lawyerEntity.getLawyerName());
        lawyerDTO.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
        lawyerDTO.setLawyerLike(lawyerEntity.getLawyerLike());
        lawyerDTO.setLawyerAnswerCnt(lawyerEntity.getLawyerAnswerCnt());
        return lawyerDTO;
    }
    private LawyerEntity converLawyerEntity(LawyerDTO lawyerDTO) {
        LawyerEntity lawyerEntity = new LawyerEntity();
        lawyerEntity.setLawyerIdx(lawyerDTO.getLawyerIdx());
        lawyerEntity.setLawyerName(lawyerDTO.getLawyerName());
        lawyerEntity.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
        lawyerEntity.setLawyerLike(lawyerDTO.getLawyerLike());
        lawyerEntity.setLawyerAnswerCnt(lawyerDTO.getLawyerAnswerCnt());
        return lawyerEntity;
    }

    public List<LawyerDTO> getRankingList() {
        List<LawyerEntity> lawyerRankingEntityList =  rankingRepository.findAll();
        return lawyerRankingEntityList.stream()
                                        .map(lawyerEntity -> convertLawyerDTO(lawyerEntity))
                                        .collect(Collectors.toList());
    }
}
