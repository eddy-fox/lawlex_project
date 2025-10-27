package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import com.soldesk.team_project.entity.BoardEntity;

public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {
    
    BoardEntity findByBoard_title(String board_title);
    BoardEntity findByBoard_titleAndBoard_content(String board_title, String board_content);
    List<BoardEntity> findByBoard_titleLike(String board_title);
    Page<BoardEntity> findAll(Pageable pageable);
    Page<BoardEntity> findAll(Specification<BoardEntity> spec, Pageable pageable);

    @Query("select "
            + "distinct q "
            + "left outer join MemberEntity u1 on q.author=u1 "
            + "left outer join ReBoardEntity a on a.boardEntity=q "
            + "left outer join MemberEntity u2 on a.author=u2 "
            + "where "
            + "   q.board_title like %:kw% "
            + "   or q.board_content like %:kw% "
            + "   or u1.memberId like %:kw% "
            + "   or a.board_content like %:kw% "
            + "   or u2.memberId like %:kw% ")
    Page<BoardEntity> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
    
}
