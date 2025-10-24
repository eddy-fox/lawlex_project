package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.MemberEntity;
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
        questionDTO.setMemberId(questionEntity.getMember().getMemberId());

        return questionDTO;
    }

    // 전체 질문글 조회
    public List<QuestionDTO> getNewQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.findByQuestionAnswerOrderByQuestionIdxDesc(qAnswer);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 새로운 질문
    public List<QuestionDTO> getCompletedQuestions(String qAnswer) { 
        List<QuestionEntity> questionEntityList = questionRepository.findByQuestionAnswerOrderByQuestionIdxDesc(qAnswer);
        
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 완료된 질문

    // 태그 별 특정 질문글 조회
    public List<QuestionDTO> searchNewQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.findByQuestionIdxAndQuestionAnswer(Integer.valueOf(keyword), qAnswer); break;
            case "title": questionEntityList = questionRepository.findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            case "content": questionEntityList = questionRepository.findByQuestionContentContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            case "id": questionEntityList = questionRepository.findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            // case "id": questionEntityList = questionRepository.findByMember_MemberIdContainingIgnoreCaseAndqAnswer(keyword, qAnswer); break;
            default: questionEntityList = questionRepository.findByQuestionAnswerOrderByQuestionIdxDesc(qAnswer); break;
        }
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 새로운 질문
    public List<QuestionDTO> searchCompletedQuestions(String searchType, String keyword, String qAnswer) {
        List<QuestionEntity> questionEntityList;

        switch (searchType) {
            case "idx": questionEntityList = questionRepository.findByQuestionIdxAndQuestionAnswer(Integer.valueOf(keyword), qAnswer); break;
            case "title": questionEntityList = questionRepository.findByQuestionTitleContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            case "content": questionEntityList = questionRepository.findByQuestionContentContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            case "id": questionEntityList = questionRepository.findByMember_MemberIdContainingIgnoreCaseAndQuestionAnswerOrderByQuestionIdxDesc(keyword, qAnswer); break;
            // case "id": questionEntityList = questionRepository.findByMember_MemberIdContainingIgnoreCaseAndqAnswer(keyword, qAnswer); break;
            default: questionEntityList = questionRepository.findByQuestionAnswerOrderByQuestionIdxDesc(qAnswer); break;
        }
        return questionEntityList.stream()
            .map(questionEntity -> convertQuestionDTO(questionEntity)).collect(Collectors.toList());
    } // 완료된 질문

}

