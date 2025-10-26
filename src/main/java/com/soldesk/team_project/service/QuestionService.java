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

    //게시글 목록 불러오기
    public Page<QuestionEntity> getList(int page, String kw) {
        
        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("questionRegDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.questionRepository.findAllByKeyword(kw, pageable);

    }

    public QuestionEntity getQuestionEntity(Integer id) {

        Optional<QuestionEntity> questionEntity = this.questionRepository.findById(id);
        if(questionEntity.isPresent()) {
            return questionEntity.get();
        } else {
            throw new DataNotFoundException("question not found");
        }
    }

    public void create(String title, String content, MemberEntity member) {

        QuestionEntity q = new QuestionEntity();
        q.setQuestionTitle(title);
        q.setQuestionContent(content);
        q.setQuestionRegDate(LocalDate.now());
        q.setAuthor(member);
        this.questionRepository.save(q);

    }

    public void modify(QuestionEntity question, String title, String content) {

        question.setQuestionTitle(title);
        question.setQuestionContent(content);
        question.setModifyDate(LocalDate.now());
        this.questionRepository.save(question);

    }

    public void delete(QuestionEntity question) {

        this.questionRepository.delete(question);

    }

    public void vote(QuestionEntity question, MemberEntity member) {

        question.getVoter().add(member);
        this.questionRepository.save(question);
        
    }
    
}

