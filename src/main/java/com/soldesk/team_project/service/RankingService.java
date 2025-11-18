package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        lawyerEntity.setLawyerImgPath(lawyerDTO.getLawyerImgPath());
        lawyerEntity.setLawyerLike(lawyerDTO.getLawyerLike());
        lawyerEntity.setLawyerAnswerCnt(lawyerDTO.getLawyerAnswerCnt());
        return lawyerEntity;
    }

    public List<LawyerDTO> getRankingList(String pick) {
        Pageable pageable = PageRequest.of(0, 10);
        List<LawyerEntity> rankingList;
        if("answer".equals(pick)){
            rankingList = rankingRepository.findAllByOrderByLawyerAnswerCntDesc(pageable);
        }else {
            rankingList = rankingRepository.findAllByOrderByLawyerLikeDesc(pageable);
        }
        return rankingList.stream()
                           .map(this :: convertLawyerDTO)
                           .collect(Collectors.toList());
    }

    public Map<String, List<Object[]>> getInterestAnswerRanking() {
        List<Object[]> rankingInterest = rankingRepository.findInterestAnswerRanking();

        Map<String, List<Object[]>> rankingMap = new LinkedHashMap<>();
        
        for(Object[] row : rankingInterest) {
            String interestName = (String) row[1];

            List<Object[]> list = rankingMap.computeIfAbsent(interestName, k -> new ArrayList<>());

            if(list.size() < 3) {
                list.add(row);
            }
        }
        return rankingMap;
    }

    /**
     * 변호사의 좋아요 순 랭킹 계산 (활성 변호사만 대상)
     */
    public int getLikeRanking(Integer lawyerIdx) {
        // 활성 변호사만 조회하여 정렬
        List<LawyerEntity> allLawyers = rankingRepository.findAll()
            .stream()
            .filter(l -> l.getLawyerActive() != null && l.getLawyerActive() == 1)
            .sorted((a, b) -> {
                Integer likeA = a.getLawyerLike() != null ? a.getLawyerLike() : 0;
                Integer likeB = b.getLawyerLike() != null ? b.getLawyerLike() : 0;
                return likeB.compareTo(likeA); // 내림차순
            })
            .collect(Collectors.toList());
        
        for (int i = 0; i < allLawyers.size(); i++) {
            if (allLawyers.get(i).getLawyerIdx().equals(lawyerIdx)) {
                return i + 1; // 1부터 시작하는 순위
            }
        }
        return 0; // 찾지 못한 경우
    }

    /**
     * 변호사의 답변수 순 랭킹 계산 (활성 변호사만 대상)
     */
    public int getAnswerRanking(Integer lawyerIdx) {
        // 활성 변호사만 조회하여 정렬
        List<LawyerEntity> allLawyers = rankingRepository.findAll()
            .stream()
            .filter(l -> l.getLawyerActive() != null && l.getLawyerActive() == 1)
            .sorted((a, b) -> {
                Integer answerA = a.getLawyerAnswerCnt() != null ? a.getLawyerAnswerCnt() : 0;
                Integer answerB = b.getLawyerAnswerCnt() != null ? b.getLawyerAnswerCnt() : 0;
                return answerB.compareTo(answerA); // 내림차순
            })
            .collect(Collectors.toList());
        
        for (int i = 0; i < allLawyers.size(); i++) {
            if (allLawyers.get(i).getLawyerIdx().equals(lawyerIdx)) {
                return i + 1; // 1부터 시작하는 순위
            }
        }
        return 0; // 찾지 못한 경우
    }
}
