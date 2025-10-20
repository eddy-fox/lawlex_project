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
        questionDTO.setQIdx(questionEntity.getQIdx());
        questionDTO.setQTitle(questionEntity.getQTitle());
        questionDTO.setQContent(questionEntity.getQContent());
        questionDTO.setQRegDate(questionEntity.getQRegDate());
        questionDTO.setQSecret(questionEntity.getQSecret());
        questionDTO.setQAnswer(questionEntity.getQAnswer());
        questionDTO.setMemberId(questionEntity.getMember().getMemberId());

        return questionDTO;
    }

    // 전체 질문글 조회
    public List<QuestionDTO> getNewQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.findByQAnswer(qAnswer);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 새로운 질문
    public List<QuestionDTO> getCompletedQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.findByQAnswer(qAnswer);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 완료된 질문

    // 태그 별 특정 질문글 조회
    public List<QuestionDTO> searchNewQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.findByQIdxAndQAnswer(Integer.valueOf(keyword), qAnswer); break;
            case "title": questionEntityList = questionRepository.findByQTitleContainingIgnoreCaseAndQAnswerOrderByQTitleAsc(keyword, qAnswer); break;
            case "content": questionEntityList = questionRepository.findByQContentContainingIgnoreCaseAndQAnwerOrderByQContentAsc(keyword, qAnswer); break;
            case "id": questionEntityList = questionRepository.findByMemberIdContainingIgnoreCaseAndQAnswerOrderByMemberIdAsc(keyword, qAnswer); break;
            default: questionEntityList = questionRepository.findByQAnswer(qAnswer); break;
        }
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 새로운 질문
    public List<QuestionDTO> searchCompletedQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.findByQIdxAndQAnswer(Integer.valueOf(keyword), qAnswer); break;
            case "title": questionEntityList = questionRepository.findByQTitleContainingIgnoreCaseAndQAnswerOrderByQTitleAsc(keyword, qAnswer); break;
            case "content": questionEntityList = questionRepository.findByQContentContainingIgnoreCaseAndQAnwerOrderByQContentAsc(keyword, qAnswer); break;
            case "id": questionEntityList = questionRepository.findByMemberIdContainingIgnoreCaseAndQAnswerOrderByMemberIdAsc(keyword, qAnswer); break;
            default: questionEntityList = questionRepository.findByQAnswer(qAnswer); break;
        }
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 완료된 질문

}
