package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.ReBoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReBoardService {
    
    private final ReBoardRepository reboardRepository;
    private final PythonService pythonService;
    private final LawyerRepository lawyerRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ReBoardEntity create(BoardEntity board, String content, LawyerEntity lawyer) {

        ReBoardEntity reboard = new ReBoardEntity();
        reboard.setReboardContent(content);
        reboard.setReboardRegDate(LocalDate.now());
        reboard.setBoardEntity(board);
        reboard.setLawyer(lawyer);
        reboard.setReboardActive(1);
        
        return this.reboardRepository.save(reboard);

    }

    public ReBoardEntity getReboard(Integer id) {

        Optional<ReBoardEntity> reboard = this.reboardRepository.findById(id);
        if(reboard.isPresent()) {
            return reboard.get();
        } else {
            throw new DataNotFoundException("reboard not found");
        }

    }

    public ReBoardEntity getReboardByBoardIdx(Integer boardIdx) {

        return this.reboardRepository.findByBoardEntityBoardIdx(boardIdx).orElse(null);

    }

    public void modify(ReBoardEntity reboard, String content) {

        reboard.setReboardContent(content);
        this.reboardRepository.save(reboard);

    }

    public void delete(ReBoardEntity reboard) {

        reboard.setReboardActive(0);
        this.reboardRepository.save(reboard);

    }

    public void vote(ReBoardEntity reboard, LawyerEntity lawyer) {

        if (reboard.getVoter() == null) {
            reboard.setVoter(new java.util.HashSet<>());
        }
        reboard.getVoter().add(lawyer);
        lawyer.setLawyerLike(lawyer.getLawyerLike() + 1);
        this.reboardRepository.save(reboard);
        this.lawyerRepository.save(lawyer);

    }

    @Transactional
    public void memberLike(ReBoardEntity reboard, MemberEntity member, Integer boardIdx) {
        // 이미 좋아요를 눌렀는지 확인 (같은 게시글의 같은 답글에 대해)
        if (reboard.getMemberVoter() == null) {
            reboard.setMemberVoter(new java.util.HashSet<>());
        }
        
        // 중복 체크: 이미 좋아요를 누른 경우 무시
        if (reboard.getMemberVoter().contains(member)) {
            return;
        }
        
        // 같은 게시글(boardIdx)의 다른 답글에 이미 좋아요를 눌렀는지 확인
        // 요구사항: 한 게시글당 한 번만 누를 수 있음
        java.util.List<ReBoardEntity> boardReboards = this.reboardRepository.findByBoardEntityBoardIdxAndReboardActive(boardIdx, 1);
        for (ReBoardEntity rb : boardReboards) {
            if (rb.getMemberVoter() != null && rb.getMemberVoter().contains(member)) {
                // 이미 이 게시글의 다른 답글에 좋아요를 눌렀음
                return;
            }
        }
        
        // 좋아요 추가
        reboard.getMemberVoter().add(member);
        
        // 변호사의 lawyerLike 증가
        if (reboard.getLawyer() != null) {
            LawyerEntity lawyer = reboard.getLawyer();
            lawyer.setLawyerLike(lawyer.getLawyerLike() + 1);
            this.lawyerRepository.save(lawyer);
        }
        
        this.reboardRepository.save(reboard);
    }

    // GPT 자동 답변 생성
    @Async("taskExecutor")
    @Transactional
    public void gptAutoReboard(BoardEntity boardEntity) {
        try {
            System.out.println("[비동기] GPT 자동 답변 생성 시작 - boardIdx: " + boardEntity.getBoardIdx() + ", Thread: " + Thread.currentThread().getName());
           
            
            // GPT API 실행
            String answer = pythonService.runPython(
                "gpt-api.py",
                boardEntity.getBoardTitle(),
                boardEntity.getInterest().getInterestName(),
                boardEntity.getBoardContent()
            );
            
            System.out.println("GPT 답변 수신 완료 - 길이: " + (answer != null ? answer.length() : 0));

            // 답변이 비어있거나 에러 메시지인 경우 체크
            if (answer == null || answer.trim().isEmpty() || answer.contains("Python 실행 실패")) {
                System.out.println("GPT 답변 생성 실패: " + answer);
                return;
            }

            // AI 변호사 조회 (lawyerIdx = 205)
            LawyerEntity aiLawyer = lawyerRepository.findByLawyerIdx(205);
            if (aiLawyer == null) {
                System.out.println("AI 변호사를 찾을 수 없습니다 (lawyerIdx=205)");
                return;
            }

            // 답변 게시글 생성
            ReBoardEntity reboardEntity = new ReBoardEntity();
            // reboardIdx는 자동 생성되므로 설정하지 않음
            reboardEntity.setBoardEntity(boardEntity);  // 중요: boardEntity 연결
            reboardEntity.setReboardContent(answer);
            reboardEntity.setReboardRegDate(LocalDate.now());
            reboardEntity.setReboardActive(1);
            reboardEntity.setLawyer(aiLawyer);

            reboardRepository.save(reboardEntity);
            System.out.println("GPT 자동 답변 저장 완료 - reboardIdx: " + reboardEntity.getReboardIdx());

        } catch (Exception e) {
            System.out.println("GPT 자동 답변 생성 중 오류 발생: " + e.getMessage());
        }
    }
    
}