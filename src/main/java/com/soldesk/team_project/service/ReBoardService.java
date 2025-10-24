package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.ReBoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ReBoardService {
    
    private final ReBoardRepository reboardRepository;

    public ReBoardEntity create(BoardEntity board, String content, MemberEntity author) {

        ReBoardEntity reboard = new ReBoardEntity();
        reboard.setReboard_content(content);
        reboard.setReboard_regDate(LocalDate.now());
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

    public void modift(ReBoardEntity reboard, String content) {

        reboard.setReboard_content(content);
        reboard.setModifyDate(LocalDate.now());
        this.reboardRepository.save(reboard);

    }

    public void delete(ReBoardEntity reboard) {

        this.reboardRepository.delete(reboard);

    }

    public void vote(ReBoardEntity reboard, MemberEntity member) {

        reboard.getVoter().add(member);
        this.reboardRepository.save(reboard);

    }
    
}
