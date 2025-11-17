package com.soldesk.team_project.service;

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
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.entity.ReBoardEntity;
import com.soldesk.team_project.repository.BoardRepository;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.entity.BoardEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.InterestEntity;

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
    private final InterestRepository interestRepository;
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

    public Page<BoardEntity> getListByInterest(int page, String kw, int interestIdx) {

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(Sort.Order.desc("boardRegDate"));
        Pageable pageable = PageRequest.of(page, 10, Sort.by(sorts));
        return this.boardRepository.findByInterestIdx(interestIdx, kw, pageable);
        
    }

    @Transactional(readOnly = true)
    public BoardEntity getBoardEntity(Integer id) {

        Optional<BoardEntity> boardEntity = this.boardRepository.findById(id);
        if(boardEntity.isPresent()) {
            BoardEntity board = boardEntity.get();
            // reboardList와 lawyer 정보를 함께 로드하기 위해 초기화
            if (board.getReboardList() != null) {
                board.getReboardList().size(); // Lazy 로딩 강제 실행
                // 각 reboard의 lawyer 정보도 로드
                board.getReboardList().forEach(reboard -> {
                    if (reboard.getLawyer() != null) {
                        reboard.getLawyer().getLawyerName(); // Lazy 로딩 강제 실행
                        reboard.getLawyer().getLawyerImgPath(); // 이미지 경로도 로드
                    }
                    if (reboard.getLawyerIdx() != null) {
                        reboard.getLawyerIdx().getLawyerName(); // Lazy 로딩 강제 실행
                        reboard.getLawyerIdx().getLawyerImgPath(); // 이미지 경로도 로드
                    }
                });
            }
            return board;
        } else {
            throw new DataNotFoundException("boardEntity not found");
        }

    }

    /**
     * 카테고리로부터 interestIdx를 결정하는 메서드
     */
    public Integer getInterestIdxFromCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return 1; // 기본값
        }
        
        // categoryIdx = 1
        if (category.equals("성매매") || category.equals("성폭력/강제추행 등") || 
            category.equals("미성년 대상 성범죄") || category.equals("디지털 성범죄")) {
            return 1;
        }
        // categoryIdx = 2
        if (category.equals("횡령/배임") || category.equals("사기/공갈") || 
            category.equals("기타 재산범죄")) {
            return 2;
        }
        // categoryIdx = 3
        if (category.equals("교통사고/도주") || category.equals("음주/무면허")) {
            return 3;
        }
        // categoryIdx = 4
        if (category.equals("고소/소송절차") || category.equals("수사/체포/구속")) {
            return 4;
        }
        // categoryIdx = 5
        if (category.equals("폭행/협박/상해 일반")) {
            return 5;
        }
        // categoryIdx = 6
        if (category.equals("명예훼손/모욕 일반") || category.equals("사이버 명예훼손/모욕")) {
            return 6;
        }
        // categoryIdx = 7
        if (category.equals("마약/도박") || category.equals("소년범죄/학교폭력") || 
            category.equals("형사일반/기타범죄")) {
            return 7;
        }
        // categoryIdx = 8
        if (category.equals("건축/부동산 일반") || category.equals("재개발/재건축") || 
            category.equals("매매/소유권 등") || category.equals("임대차")) {
            return 8;
        }
        // categoryIdx = 9
        if (category.equals("손해배상") || category.equals("대여금/채권추심") || 
            category.equals("계약일반/매매")) {
            return 9;
        }
        // categoryIdx = 10
        if (category.equals("소송/집행절차") || category.equals("가압류/가처분") || 
            category.equals("회생/파산")) {
            return 10;
        }
        // categoryIdx = 11
        if (category.equals("공증/내용증명/조합/국제문제 등")) {
            return 11;
        }
        // categoryIdx = 12
        if (category.equals("이혼") || category.equals("상속") || category.equals("가사 일반")) {
            return 12;
        }
        // categoryIdx = 13
        if (category.equals("기업법무") || category.equals("노동/인사")) {
            return 13;
        }
        // categoryIdx = 14
        if (category.equals("세금/행정/헌법") || category.equals("의료/식품의약") || 
            category.equals("병역/군형법")) {
            return 14;
        }
        // categoryIdx = 15
        if (category.equals("소비자/공정거래") || category.equals("IT/개인정보") || 
            category.equals("지식재산권/엔터") || category.equals("금융/보험")) {
            return 15;
        }
        
        return 1; // 기본값
    }

    public void create(String boardTitle, String boardContent, String boardCategory, Integer interestIdx, MemberEntity member) {

        BoardEntity q = new BoardEntity();
        q.setBoardTitle(boardTitle);
        q.setBoardContent(boardContent);
        q.setBoardCategory(boardCategory);
        q.setBoardRegDate(LocalDate.now());
        q.setMember(member);
        q.setBoardActive(1); // 기본값 1 (활성)
        
        // interestIdx 설정: null이거나 유효하지 않으면 카테고리로부터 자동 결정
        if (interestIdx == null || interestIdx <= 0) {
            interestIdx = getInterestIdxFromCategory(boardCategory);
        }
        
        Optional<InterestEntity> interest = interestRepository.findById(interestIdx);
        if (interest.isPresent()) {
            q.setInterest(interest.get());
        } else {
            // interestIdx가 유효하지 않으면 카테고리로부터 다시 계산
            interestIdx = getInterestIdxFromCategory(boardCategory);
            Optional<InterestEntity> defaultInterest = interestRepository.findById(interestIdx);
            if (defaultInterest.isPresent()) {
                q.setInterest(defaultInterest.get());
            }
        }
        
        this.boardRepository.save(q);

    }

    public void modify(BoardEntity boardEntity, String boardTitle, String boardContent) {

        boardEntity.setBoardTitle(boardTitle);
        boardEntity.setBoardContent(boardContent);
        this.boardRepository.save(boardEntity);

    }

    public void delete(BoardEntity boardEntity) {

        // 소프트 삭제: board_active를 0으로 설정
        boardEntity.setBoardActive(0);
        this.boardRepository.save(boardEntity);

    }

}