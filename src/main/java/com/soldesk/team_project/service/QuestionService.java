package com.soldesk.team_project.service;

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
        questionDTO.setQSecret(questionEntity.getQuestionSecret());
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
    public List<QuestionDTO> getNewQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.
            findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(qAnswer, 1);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 새로운 질문
    public List<QuestionDTO> getCompletedQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.
            findByQuestionAnswerAndQuestionActiveOrderByQuestionIdxDesc(qAnswer, 1);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 완료된 질문

    // 태그 별 특정 질문글 조회
    public List<QuestionDTO> searchNewQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.
                findByQuestionIdxAndQuestionAnswerAndQuestionActive(Integer.valueOf(keyword), qAnswer, 1); break;
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
    } // 새로운 질문
    public List<QuestionDTO> searchCompletedQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.
                findByQuestionIdxAndQuestionAnswerAndQuestionActive(Integer.valueOf(keyword), qAnswer, 1); break;
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
    } // 완료된 질문

}

