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

        System.out.println("========== ReBoardService.create 시작 ==========");
        System.out.println("게시글 ID: " + board.getBoardIdx());
        System.out.println("답변 내용: " + content);
        System.out.println("변호사 ID: " + lawyer.getLawyerIdx());
        
        ReBoardEntity reboard = new ReBoardEntity();
        reboard.setReboardContent(content);
        reboard.setReboardRegDate(LocalDate.now());
        reboard.setBoardEntity(board);
        reboard.setLawyer(lawyer);
        reboard.setReboardActive(1);
        
        System.out.println("답변 엔티티 생성 완료, 저장 시작...");
        ReBoardEntity saved = this.reboardRepository.save(reboard);
        System.out.println("========== ReBoardService.create 저장 완료 ==========");
        System.out.println("저장된 답변 ID: " + saved.getReboardIdx());
        System.out.println("저장된 답변 내용: " + saved.getReboardContent());
        System.out.println("저장된 답변 활성화 상태: " + saved.getReboardActive());
        
        return saved;

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
    @Async
    @Transactional
    public void gptAutoReboard(BoardEntity boardEntity) {

        try {
            // GPT API 실행
            String answer = pythonService.runPython(
                "gpt-api.py",
                boardEntity.getBoardTitle(),
                boardEntity.getInterest().getInterestName(),
                boardEntity.getBoardContent()
            );

            // 답변 게시글 생성            
            ReBoardEntity reboardEntity = new ReBoardEntity();
            reboardEntity.setReboardContent(answer);
            reboardEntity.setReboardActive(1);
            reboardEntity.setBoardEntity(boardEntity);
            LawyerEntity lawyer = lawyerRepository.findByLawyerIdx(205);
            reboardEntity.setLawyer(lawyer);

            reboardRepository.save(reboardEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}