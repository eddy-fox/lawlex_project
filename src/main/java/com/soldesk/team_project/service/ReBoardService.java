package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.ReboardDTO;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.ReBoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReBoardService {
    
    private final ReBoardRepository reboardRepository;
    private final pythonService pythonService;

    public ReBoardEntity create(BoardEntity board, String content, MemberEntity author) {

        ReBoardEntity reboard = new ReBoardEntity();
        reboard.setReboardContent(content);
        reboard.setReboardRegDate(LocalDate.now());
        reboard.setBoard(board);
        reboard.setAuthor(author);
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

    public void modify(ReBoardEntity reboard, String content) {

        reboard.setReboardContent(content);
        reboard.setModifyDate(LocalDate.now());
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
                boardEntity.getInterest().getInterestName(),
                boardEntity.getBoardContent()
            );

            // 답변 게시글 생성
            // ReBoardEntity reboardEntity = new ReboardEntity();
            // reboardEntity.setBoardIdx(boardEntity.getBoardIdx());
            // reboardEntity.setReboardTitle("GPT가 작성한 답변입니다.");
            // reboardEntity.setReboardContent(answer);
            // reboardEntity.setLawyerIdx(0);

            // reboardRepository.save(reboardEntity);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
    
}