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
    
    BoardEntity findByBoardTitle(String boardTitle);
    BoardEntity findByBoardTitleAndBoardContent(String boardTitle, String boardContent);
    List<BoardEntity> findByBoardTitleLike(String boardTitle);
    Page<BoardEntity> findAll(Pageable pageable);
    Page<BoardEntity> findAll(Specification<BoardEntity> spec, Pageable pageable);

    @Query("""
    select distinct q
    from BoardEntity q
    left join q.author u1
    left join q.reboardList a
    left join a.author u2
    where
        lower(q.boardTitle)   like lower(concat('%', :kw, '%'))
    or lower(q.boardContent) like lower(concat('%', :kw, '%'))
    or (u1.memberId is not null and lower(u1.memberId) like lower(concat('%', :kw, '%')))
    or (a.reboardContent is not null and lower(a.reboardContent) like lower(concat('%', :kw, '%')))
    or (u2.memberId is not null and lower(u2.memberId) like lower(concat('%', :kw, '%')))
    """)
    Page<BoardEntity> findAllByKeyword(@Param("kw") String kw, Pageable pageable);

}
