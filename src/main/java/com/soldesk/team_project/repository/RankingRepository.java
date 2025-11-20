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
    List<Object[]> findInterestAnswerRanking();   // 결과는 Object[] 리스트로 받기

}
