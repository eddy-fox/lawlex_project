package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    private QuestionDTO convertQuestionDTO (QuestionEntity questionEntity) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setQIdx(questionEntity.getQuestionIdx());
        questionDTO.setQTitle(questionEntity.getQuestionTitle());
        questionDTO.setQContent(questionEntity.getQuestionContent());
        questionDTO.setQRegDate(questionEntity.getQuestionRegDate());
        questionDTO.setQSecret("Y".equalsIgnoreCase(questionEntity.getQuestionSecret()) ? "🔒" : "🔓");
        questionDTO.setQAnswer(questionEntity.getQuestionAnswer());
        questionDTO.setQActive(questionEntity.getQuestionActive());
        
        if (questionEntity.getMemberIdx() != null) {
            questionDTO.setMemberIdx(questionEntity.getMemberIdx());
        } else {
            questionDTO.setMemberIdx(null);
        }

        if (questionEntity.getLawyerIdx() != null) {
            questionDTO.setLawyerIdx(questionEntity.getLawyerIdx());
        } else {
            questionDTO.setLawyerIdx(null);
        }

        return questionDTO;
    }

    // 전체 질문글 조회
    public List<QuestionDTO> getQuestions(int qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.
            findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(qAnswer, 1);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 질문글 조회
    public List<QuestionDTO> searchQuestions(String searchType, String keyword, int qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx":
                try {
                    int idx = Integer.parseInt(keyword);
                    questionEntityList = questionRepository.
                        findByQuestionIdxAndQuestionAnswerAndQuestionActive(idx, qAnswer, 1);
                } catch (NumberFormatException e) {
                    questionEntityList = new ArrayList<>();
                } break;
            case "title": questionEntityList = questionRepository.
                findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(keyword, qAnswer, 1); break;
            case "content": questionEntityList = questionRepository.
                findByQuestionContentContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(keyword, qAnswer, 1); break;
            case "id": questionEntityList = questionRepository.
                findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(keyword, qAnswer, 1); break;
            default: questionEntityList = questionRepository.
                findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(qAnswer, 1); break;
        }
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    }
    
}

