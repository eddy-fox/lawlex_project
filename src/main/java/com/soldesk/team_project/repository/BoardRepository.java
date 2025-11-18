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
        (q.boardActive = 1 or q.boardActive is null)
        and (
            lower(q.boardTitle)   like lower(concat('%', :kw, '%'))
            or lower(q.boardContent) like lower(concat('%', :kw, '%'))
            or (m.memberId is not null and lower(m.memberId) like lower(concat('%', :kw, '%')))
        )
    """)
    Page<BoardEntity> findAllByKeyword(@Param("kw") String kw, Pageable pageable);
    
    @Query("SELECT b FROM BoardEntity b WHERE (b.boardActive = 1 OR b.boardActive IS NULL) " +
           "AND (:interestIdx IS NULL OR b.interest.interestIdx = :interestIdx) " + 
           "AND (:keyword IS NULL OR LOWER(b.boardTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<BoardEntity> findByInterestIdx(@Param("interestIdx") Integer interestIdx,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);
    
    // 조회수 높은 순서로 조회 (상위 5개, 활성 게시물만)
    @Query("SELECT b FROM BoardEntity b WHERE (b.boardActive = 1 OR b.boardActive IS NULL) ORDER BY b.boardViews DESC")
    List<BoardEntity> findTop5ActiveBoardsByOrderByBoardViewsDesc(Pageable pageable);
    
    // 관심 카테고리별 조회수 높은 순서로 조회 (상위 5개, 활성 게시물만)
    // 기존 findTop5ActiveBoardsByOrderByBoardViewsDesc에 관심 카테고리 조건 추가
    @Query("SELECT b FROM BoardEntity b WHERE (b.boardActive = 1 OR b.boardActive IS NULL) " +
           "AND (b.interest.interestIdx IN (:interestIdxList)) " +
           "ORDER BY b.boardViews DESC")
    List<BoardEntity> findTop5ActiveBoardsByInterestIdxOrderByBoardViewsDesc(
        @Param("interestIdxList") List<Integer> interestIdxList,
        Pageable pageable);
                                        
    // 조회수 높은 순서로 조회 (상위 5개)
    List<BoardEntity> findTop5ByOrderByBoardViewsDesc();


           
}
