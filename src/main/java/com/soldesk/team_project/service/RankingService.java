package com.soldesk.team_project.service;

import java.sql.Date;
import java.time.LocalDate;
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
        lawyerDTO.setLawyerActive(lawyerEntity.getLawyerActive());
        return lawyerDTO;
    }
    public List<LawyerDTO> getRankingList(String pick) {
        Pageable pageable = PageRequest.of(0, 10);
        List<LawyerEntity> rankingList;
        if("answer".equals(pick)){
            rankingList = rankingRepository.findAllByLawyerActiveOrderByLawyerAnswerCntDesc(1, pageable);
        }else {
            rankingList = rankingRepository.findAllByLawyerActiveOrderByLawyerLikeDesc(1, pageable);
        }
        return rankingList.stream()
                           .map(this :: convertLawyerDTO)
                           .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTopLikedAnswers(int limit) {
        List<Object[]> allRows = rankingRepository.findTopLikedAnswersNative();
        // limit만큼만 가져오기
        List<Object[]> rows = allRows.stream()
                                     .limit(limit)
                                     .collect(Collectors.toList());
        List<Map<String, Object>> bestAnswers = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("reboardIdx", row[0]);
            item.put("boardIdx", row[1]);
            item.put("boardTitle", row[2]);
            item.put("boardCategory", row[3]);
            item.put("lawyerIdx", row[4]);
            item.put("lawyerName", row[5]);
            item.put("lawyerImgPath", row[6]);

            String content = row[7] != null ? row[7].toString() : "";
            item.put("answerPreview", buildPreview(content));

            LocalDate answerDate = null;
            Object dateObj = row[8];
            if (dateObj instanceof LocalDate ld) {
                answerDate = ld;
            } else if (dateObj instanceof Date sqlDate) {
                answerDate = sqlDate.toLocalDate();
            }
            item.put("answerDate", answerDate);

            Number likeCount = row[9] instanceof Number ? (Number) row[9] : Integer.valueOf(0);
            item.put("likeCount", likeCount.intValue());

            bestAnswers.add(item);
        }

        return bestAnswers;
    }

    private String buildPreview(String content) {
        if (content == null) {
            return "";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 130) {
            return normalized;
        }
        return normalized.substring(0, 127) + "...";
    }

/*     public Map<String, List<Object[]>> getInterestAnswerRanking() {
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
    } */

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
