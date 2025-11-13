package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;

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

    private QuestionEntity convertQuestionEntity(QuestionDTO questionDTO) {
        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setQuestionIdx(questionDTO.getQIdx());
        questionEntity.setQuestionTitle(questionDTO.getQTitle());
        questionEntity.setQuestionContent(questionDTO.getQContent());
        questionEntity.setQuestionRegDate(questionDTO.getQRegDate());
        questionEntity.setQuestionSecret(questionDTO.getQSecret());
        questionEntity.setQuestionAnswer(questionDTO.getQAnswer());
        questionEntity.setQuestionActive(questionEntity.getQuestionActive());

        MemberEntity memberEntity = memberRepository.findById(questionDTO.getMemberIdx()).orElse(null);
        LawyerEntity lawyerEntity = lawyerRepository.findById(questionDTO.getLawyerIdx()).orElse(null);

        questionEntity.setMemberIdx(memberEntity.getMemberIdx());
        questionEntity.setLawyerIdx(lawyerEntity.getLawyerIdx());
        
        return questionEntity;
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
    

    public Page<QuestionDTO> getQnaPaging(int page){
        int p = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(p, 10);
        return questionRepository.findAllByOrderByQuestionRegDateDesc(pageable)
        .map(this::convertQuestionDTO);
    }

    public void qnaWriting(QuestionDTO qnaWrite){
        QuestionEntity questionEntity = convertQuestionEntity(qnaWrite);
        questionRepository.save(questionEntity);
    }
}

