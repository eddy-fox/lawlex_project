package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.ReBoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReBoardService {
    
    private final ReBoardRepository reboardRepository;
    private final PythonService pythonService;
    private final LawyerRepository lawyerRepository;

    public ReBoardEntity create(BoardEntity board, String content, LawyerEntity lawyer) {

        ReBoardEntity reboard = new ReBoardEntity();
        reboard.setReboardContent(content);
        reboard.setReboardRegDate(LocalDate.now());
        reboard.setBoard(board);
        reboard.setLawyer(lawyer);
        this.reboardRepository.save(reboard);
        return reboard;

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

        return this.reboardRepository.findByBoardBoardIdx(boardIdx).orElse(null);

    }

    public void modify(ReBoardEntity reboard, String content) {

        reboard.setReboardContent(content);
        this.reboardRepository.save(reboard);

    }

    public void delete(ReBoardEntity reboard) {

        this.reboardRepository.delete(reboard);

    }

    // GPT 자동 답변 생성
    @Transactional
    public void gptAutoReboard(BoardEntity boardEntity) {

        try {
            // GPT API 실행
            String answer = pythonService.runPython(
                "gpt-api.py",
                boardEntity.getBoardTitle(),
                // boardEntity.getInterest().getInterestName(),
                boardEntity.getBoardContent()
            );

            // 답변 게시글 생성            
            ReBoardEntity reboardEntity = new ReBoardEntity();
            reboardEntity.setReboardIdx(boardEntity.getBoardIdx());
            reboardEntity.setReboardTitle("GPT가 작성한 답변입니다.");
            reboardEntity.setReboardContent(answer);

            reboardRepository.save(reboardEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void vote(ReBoardEntity reboard, MemberEntity member) {

        reboard.getVoter().add(member);
        this.reboardRepository.save(reboard);
        
    }
    
}