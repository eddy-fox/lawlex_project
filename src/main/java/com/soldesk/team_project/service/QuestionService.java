package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.AnswerDTO;
import com.soldesk.team_project.dto.QuestionDTO;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.entity.AnswerEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.QuestionEntity;
import com.soldesk.team_project.repository.AdminRepository;
import com.soldesk.team_project.repository.AnswerRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.QuestionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class QuestionService {


    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;
    private final AdminRepository adminRepository;

    private QuestionDTO convertQuestionDTO (QuestionEntity questionEntity) {
        QuestionDTO questionDTO = new QuestionDTO();
        questionDTO.setQIdx(questionEntity.getQuestionIdx());
        questionDTO.setQTitle(questionEntity.getQuestionTitle());
        questionDTO.setQContent(questionEntity.getQuestionContent());
        questionDTO.setQRegDate(questionEntity.getQuestionRegDate());
        questionDTO.setQSecret(questionEntity.getQuestionSecret());
        questionDTO.setQAnswer(questionEntity.getQuestionAnswer());
        questionDTO.setQActive(questionEntity.getQuestionActive());
        questionDTO.setMemberIdx(questionEntity.getMemberIdx());
        questionDTO.setLawyerIdx(questionEntity.getLawyerIdx());
        
        // 작성자 ID 설정 (member 또는 lawyer) - 수동 조회
        if (questionEntity.getLawyerIdx() != null) {
            lawyerRepository.findById(questionEntity.getLawyerIdx()).ifPresent(lawyer -> {
                questionDTO.setInfoId(lawyer.getLawyerId());
                questionDTO.setInfoName(lawyer.getLawyerName());
            });
        } else if (questionEntity.getMemberIdx() != null) {
            memberRepository.findById(questionEntity.getMemberIdx()).ifPresent(member -> {
                questionDTO.setInfoId(member.getMemberId());
                questionDTO.setInfoName(member.getMemberName());
            });
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


        MemberEntity memberEntity = memberRepository.findById(questionDTO.getMemberIdx()).orElse(null);
        LawyerEntity lawyerEntity = lawyerRepository.findById(questionDTO.getLawyerIdx()).orElse(null);

        questionEntity.setMember(memberEntity);
        questionEntity.setLawyer(lawyerEntity);

        return questionEntity;
    }

    private AnswerDTO convertAnswerDTO(AnswerEntity answerEntity) {
        AnswerDTO answerDTO = new AnswerDTO();
        answerDTO.setAIdx(answerEntity.getAnswerIdx());
        answerDTO.setAContent(answerEntity.getAnswerContent());
        answerDTO.setARegDate(answerEntity.getAnswerRegDate());
        answerDTO.setAActive(answerEntity.getAnswerActive());
        answerDTO.setQIdx(answerEntity.getQuestionIdx());
        answerDTO.setAdminIdx(answerEntity.getAdminIdx());

        return answerDTO;
    }

    private AnswerEntity convertAnswerEntity(AnswerDTO answerDTO) {
        AnswerEntity answerEntity = new AnswerEntity();
        answerEntity.setAnswerIdx(answerDTO.getAIdx());
        answerEntity.setAnswerContent(answerDTO.getAContent());
        answerEntity.setAnswerRegDate(answerDTO.getARegDate());
        answerEntity.setAnswerActive(answerDTO.getAActive());
        answerEntity.setQuestionIdx(answerDTO.getQIdx());
        answerEntity.setAdminIdx(answerDTO.getAdminIdx());

        QuestionEntity questionEntity = questionRepository.findById(answerDTO.getQIdx()).orElse(null);
        AdminEntity adminEntity = adminRepository.findById(answerDTO.getAdminIdx()).orElse(null);

        answerEntity.setQuestion(questionEntity);
        answerEntity.setAdmin(adminEntity);

        return answerEntity;
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
    
    
    /* 모든 문의 보기 */
    public Page<QuestionDTO> getQnaPaging(int page){
        int p = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(p, 10);
        return questionRepository.findAllByOrderByQuestionRegDateDescQuestionIdxDesc(pageable)
        .map(this::convertQuestionDTO);
    }
    /* 모든 문의 검색 */
    public Page<QuestionDTO> getQnaPaging(int page, String search){
        int p = Math.max(page, 1) - 1;
        Pageable pageable = PageRequest.of(p, 10);
        
        Page<QuestionEntity> qPage;

        if (search == null || search.trim().isEmpty()){
            qPage = questionRepository.findAllByOrderByQuestionRegDateDescQuestionIdxDesc(pageable);
        }else{
            qPage = questionRepository.findByQuestionTitleContainingOrQuestionContentContainingOrderByQuestionRegDateDescQuestionIdxDesc(search, search, pageable);
        }
        
        return qPage.map(this::convertQuestionDTO);
    }

    /* 일반 회원 자기문의 보기 */
    public Page<QuestionDTO> getQnaPagingM(Integer mIdx, int page){
        int p = Math.max(page,1)-1;
        Pageable pageable = PageRequest.of(p,10);
        return questionRepository.findByMemberIdxOrderByQuestionRegDateDescQuestionIdxDesc(mIdx, pageable)
        .map(this::convertQuestionDTO);
    }
    /* 일반 회원 자기문의 검색 */
    public Page<QuestionDTO> getQnaPagingM(Integer mIdx, int page, String search){
        int p = Math.max(page,1)-1;
        Pageable pageable = PageRequest.of(p,10);

        Page<QuestionEntity> qPageM;

        if(search == null || search.trim().isEmpty()){
            qPageM = questionRepository.findByMemberIdxOrderByQuestionRegDateDescQuestionIdxDesc(mIdx, pageable);
        }else {
            qPageM = questionRepository.searchMemberQuestions(mIdx, search.trim(), pageable);
        }

        return qPageM.map(this::convertQuestionDTO);
    }
    /* 변호사회원 자기문의 보기 */
    public Page<QuestionDTO> getQnaPagingL(Integer lIdx, int page){
        int p = Math.max(page,1)-1;
        Pageable pageable = PageRequest.of(p,10);
        return questionRepository.findByLawyerIdxOrderByQuestionRegDateDescQuestionIdxDesc(lIdx, pageable)
        .map(this::convertQuestionDTO);
    }
    /* 변호사회원 자기문의 검색 */
    public Page<QuestionDTO> getQnaPagingL(Integer lIdx, int page, String search){
        int p = Math.max(page,1)-1;
        Pageable pageable = PageRequest.of(p,10);

        Page<QuestionEntity> qPageL;

        if(search == null || search.trim().isEmpty()){
            qPageL = questionRepository.findByLawyerIdxOrderByQuestionRegDateDescQuestionIdxDesc(lIdx, pageable);
        }else {
            qPageL = questionRepository.searchLawyerQuestions(lIdx, search, pageable);
        }
        
        return qPageL.map(this::convertQuestionDTO);
    }

    /* 문의 글쓰기 */
    public void qnaWriting(QuestionDTO qnaWrite){

        if (qnaWrite.getQRegDate() == null) {
            qnaWrite.setQRegDate(java.time.LocalDate.now());
        }
        if(qnaWrite.getQSecret() == null) {
            qnaWrite.setQSecret(0);
        }
        if (qnaWrite.getQAnswer() == null) {
            qnaWrite.setQAnswer(0);
        }
        if (qnaWrite.getQActive() == null) {
            qnaWrite.setQActive(1);
        }
        
        QuestionEntity questionEntity = convertQuestionEntity(qnaWrite);
        questionRepository.save(questionEntity);
    }
    
    public QuestionDTO getQ(int qIdx){
        QuestionEntity questionEntity = questionRepository.findById(qIdx).orElse(null);
        QuestionDTO questionDTO = convertQuestionDTO(questionEntity);
     return questionDTO;
    }
    


    // QIdx로 답변 찾기
    public AnswerDTO getAnswerToQIdx(int qIdx) {

        AnswerEntity answerEntity = answerRepository.findByQuestionIdxAndAnswerActive(qIdx, 1);
        if (answerEntity == null) {
            return null;
        }

        AnswerDTO answerDTO = convertAnswerDTO(answerEntity);

        return answerDTO;
    }

    // AIdx로 문의글 찾기
    public QuestionDTO getQuestionToAIdx(int aIdx) {
        QuestionEntity questionEntity = questionRepository.findByAnswerAnswerIdx(aIdx);
        QuestionDTO questionDTO = convertQuestionDTO(questionEntity);

        return questionDTO;
    }

    // AIdx로 답변 찾기
    public AnswerDTO getAnswerToAIdx(int aIdx) {
        AnswerEntity answerEntity = answerRepository.findById(aIdx).orElse(null);
        AnswerDTO answerDTO = convertAnswerDTO(answerEntity);

        return answerDTO;
    }

    // 문의글 답변 등록
    @Transactional
    public void answerProcess(AnswerDTO answerWrite) {
        
        // 답변 저장
        AnswerEntity answerEntity = convertAnswerEntity(answerWrite);
        answerEntity.setAnswerActive(1);
        answerRepository.save(answerEntity);

        // 답변 여부 변경
        QuestionEntity questionEntity = questionRepository.findById(answerWrite.getQIdx()).orElse(null);
        questionEntity.setQuestionAnswer(1);
        questionRepository.save(questionEntity);
    }

    // 답변 수정
    @Transactional
    public void modifyAnswer(AnswerDTO answerModify) {
        AnswerEntity answerEntity = answerRepository.findById(answerModify.getAIdx()).orElse(null);
        answerEntity.setAnswerContent(answerModify.getAContent());
        answerRepository.save(answerEntity);
    }

    // 답변 삭제
    @Transactional
    public void deleteAnswer(int aIdx) {
        
        // 답변 비활성화
        AnswerEntity answerEntity = answerRepository.findById(aIdx).orElse(null);
        answerEntity.setAnswerActive(0);
        answerRepository.save(answerEntity);

        // 문의글 답변 여부 변경
        QuestionEntity questionEntity = questionRepository.findByAnswerAnswerIdx(aIdx);
        questionEntity.setQuestionAnswer(0);
        questionRepository.save(questionEntity);
    
    }

    // 문의글 수정
    @Transactional
    public void modifyQuestion(QuestionDTO questionModify) {
        QuestionEntity questionEntity = questionRepository.findById(questionModify.getQIdx()).orElse(null);
        if (questionEntity != null) {
            questionEntity.setQuestionTitle(questionModify.getQTitle());
            questionEntity.setQuestionContent(questionModify.getQContent());
            questionEntity.setQuestionSecret(questionModify.getQSecret());
            questionRepository.save(questionEntity);
        }
    }

    // 문의글 삭제 (소프트 삭제: q_active를 0으로 설정)
    @Transactional
    public void deleteQuestion(int qIdx) {
        QuestionEntity questionEntity = questionRepository.findById(qIdx).orElse(null);
        if (questionEntity != null) {
            questionEntity.setQuestionActive(0);
            questionRepository.save(questionEntity);
        }
    }
    
}
