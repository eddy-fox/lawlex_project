package com.soldesk.team_project.service;

import java.lang.reflect.Member;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.soldesk.team_project.DataNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class BoardService {

    private final BoardRepository boardRepository;
    private final ReBoardService reboardService;

    private Specification<ReBoardEntity> search(String kw) {

        return new Specification<>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Predicate toPredicate(Root<ReBoardEntity> q, CriteriaQuery<?> query, CriteriaBuilder cb) {
                query.distinct(true);
                Join<BoardEntity, MemberEntity> u1 = q.join("author", JoinType.LEFT);
                Join<BoardEntity, ReBoardEntity> a = q.join("answerList", JoinType.LEFT);
                Join<ReBoardEntity, MemberEntity> u2 = a.join("author", JoinType.LEFT);
                return cb.or(cb.like(q.get("boardTitle"), "%" + kw + "%"),
                        cb.like(q.get("boardContent"), "%" + kw + "%"),
                        cb.like(u1.get("memberId"), "%" + kw + "%"),
                        cb.like(a.get("boardContent"), "%" + kw + "%"),
                        cb.like(u2.get("memberId"), "%" + kw + "%"));
            }
        };
    }

    public Page<BoardEntity> getList(int page, String kw) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("boardRegDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.boardRepository.findAllByKeyword(kw, pageable);
        
    }

    public BoardEntity getBoardEntity(Integer id) {

        Optional<BoardEntity> boardEntity = this.boardRepository.findById(id);
        if(boardEntity.isPresent()) {
            return boardEntity.get();
        } else {
            throw new DataNotFoundException("boardEntity not found");
        }

    }

    public void create(String boardTitle, String boardContent, MemberEntity member) {

        BoardEntity q = new BoardEntity();
        q.setBoardTitle(boardTitle);
        q.setBoardContent(boardContent);
        q.setBoardRegDate(LocalDate.now());
        q.setAuthor(member);
        this.boardRepository.save(q);

        // GPT 자동 답변 생성
        reboardService.gptAutoReboard(q);

    }

    public void modify(BoardEntity boardEntity, String boardTitle, String boardContent) {

        boardEntity.setBoardTitle(boardTitle);
        boardEntity.setBoardContent(boardContent);
        this.boardRepository.save(boardEntity);

    }

    public void delete(BoardEntity boardEntity) {

        this.boardRepository.delete(boardEntity);

    }

}