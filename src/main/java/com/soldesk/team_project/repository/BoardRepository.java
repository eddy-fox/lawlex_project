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
    select q
    from BoardEntity q
    left join q.member m
    where
        lower(q.boardTitle)   like lower(concat('%', :kw, '%'))
        or lower(q.boardContent) like lower(concat('%', :kw, '%'))
        or (m.memberId is not null and lower(m.memberId) like lower(concat('%', :kw, '%')))
    """)
    Page<BoardEntity> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
    
    @Query("SELECT b FROM BoardEntity b WHERE (:interestIdx IS NULL OR b.interest.interestIdx = :interestIdx) " + 
           "AND (:keyword IS NULL OR b.boardTitle LIKE %:keyword%)")
    Page<BoardEntity> findByInterestIdx(@Param("interestIdx") Integer interestIdx,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
    
    // 조회수 높은 순서로 조회 (상위 5개)
    List<BoardEntity> findTop5ByOrderByBoardViewsDesc();


           
}
