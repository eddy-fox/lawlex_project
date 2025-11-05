package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public List<LawyerDTO> getRankingList(String pick) {
        String picked = pick;
        Pageable pageable = PageRequest.of(0, 10);
        List<LawyerEntity> rankingLikeList = rankingRepository.findAllByOrderByLawyerLikeDesc(pageable);
        List<LawyerEntity> rankingAnswerCntList = rankingRepository.findAllByOrderByLawyerAnswerCntDesc(pageable);
        // if (pick.equals("like")) {
        //     return rankingLikeList.stream()
        //                     .map(lawyerEntity -> convertLawyerDTO(lawyerEntity))
        //                     .collect(Collectors.toList());
        // }else if(pick.equals("answer")) {
        //     return rankingAnswerCntList.stream()
        //                     .map(lawyerEntity -> convertLawyerDTO(lawyerEntity))
        //                     .collect(Collectors.toList());
        // };
        return null;
    }
}
