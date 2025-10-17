package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.repository.QuestionRepositiory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepositiory questionRepositiory;

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

    public List<QuestionDTO> getNewQuestions() {
        List<QuestionEntity> questionEntity = questionRepository.findAll();
        
        return memberEntityList.stream()
            .map(questionEntity -> convertMemberDTO(memberEntity)).collect(Collectors.toList());
    }
    
}
