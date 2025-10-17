package com.soldesk.team_project.service;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.repository.QuestionRepositiory;

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
    
}
