package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.soldesk.team_project.entity.LawyerEntity;

public interface RankingRepository extends JpaRepository<LawyerEntity, Integer> {
    
    List<LawyerEntity> findAllByLawyerActiveOrderByLawyerLikeDesc(Integer lawyerActive, Pageable pageable);
    List<LawyerEntity> findAllByLawyerActiveOrderByLawyerAnswerCntDesc(Integer lawyerActive, Pageable pageable);

    @Query(value = """
            SELECT
                r.reboard_idx,
                b.board_idx,
                b.board_title,
                b.board_category,
                l.lawyer_idx,
                l.lawyer_name,
                l.lawyer_img_path,
                r.reboard_content,
                r.reboard_reg_date,
                COUNT(rv.re_board_entity_reboard_idx) AS like_cnt
            FROM reboard r
            JOIN board b ON r.board_idx = b.board_idx
            JOIN lawyer l ON r.lawyer_idx = l.lawyer_idx
            LEFT JOIN reboard_member_voter rv ON r.reboard_idx = rv.re_board_entity_reboard_idx
            WHERE (r.reboard_active = 1 OR r.reboard_active IS NULL)
              AND (b.board_active = 1 OR b.board_active IS NULL)
              AND (l.lawyer_active = 1 OR l.lawyer_active IS NULL)
              AND l.lawyer_idx <> 205
            GROUP BY
                r.reboard_idx,
                b.board_idx,
                b.board_title,
                b.board_category,
                l.lawyer_idx,
                l.lawyer_name,
                l.lawyer_img_path,
                r.reboard_content,
                r.reboard_reg_date
            ORDER BY like_cnt DESC, r.reboard_reg_date DESC
            """, nativeQuery = true)
    List<Object[]> findTopLikedAnswersNative();

/*     @Query(value = """
                        SELECT
                            i.interest_idx,
                            i.interest_name,
                            l.lawyer_idx,
                            l.lawyer_name,
                            l.lawyer_img_path,
                            l.lawyer_like,
                            COUNT(*) AS answer_cnt
                        FROM board   b
                        JOIN reboard r ON b.board_idx  = r.board_idx
                        JOIN lawyer  l ON r.lawyer_idx = l.lawyer_idx
                        JOIN interest i ON b.interest_idx = i.interest_idx
                        WHERE l.lawyer_active = 1
                        GROUP BY
                            i.interest_idx,
                            i.interest_name,
                            l.lawyer_idx,
                            l.lawyer_name,
                            l.lawyer_img_path,
                            l.lawyer_like
                        ORDER BY
                            i.interest_idx,
                            answer_cnt DESC
                                            """,
                                            nativeQuery = true)
    List<Object[]> findInterestAnswerRanking();   // 결과는 Object[] 리스트로 받기  카테고리별 랭킹 사용 안함함  */

}
