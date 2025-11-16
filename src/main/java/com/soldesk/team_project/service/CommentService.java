package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.CommentDTO;
import com.soldesk.team_project.entity.CommentEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.repository.CommentRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.NewsBoardRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CommentService {
    
    private final CommentRepository commentRepository;
    private final NewsBoardRepository newsBoardRepository;
    private final MemberRepository memberRepository;
    private final LawyerRepository lawyerRepository;

    // Comment Entity -> DTO 변환
    private CommentDTO convertCommentDTO(CommentEntity commentEntity) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentIdx(commentEntity.getCommentIdx());
        commentDTO.setCommentContent(commentEntity.getCommentContent());
        commentDTO.setCommentRegDate(commentEntity.getCommentRegDate());
        commentDTO.setNewsIdx(commentEntity.getNewsIdx());
        
        // member_idx와 lawyer_idx 직접 가져오기
        Integer memberIdx = commentEntity.getMemberIdx();
        Integer lawyerIdx = commentEntity.getLawyerIdx();
        commentDTO.setMemberIdx(memberIdx);
        commentDTO.setLawyerIdx(lawyerIdx);
        
        commentDTO.setCommentActive(commentEntity.getCommentActive());
        
        // 닉네임 설정: member 또는 lawyer의 닉네임 (닉네임이 없으면 이름 사용)
        if (commentEntity.getMember() != null) {
            try {
                String nickname = commentEntity.getMember().getMemberNickname();
                if (nickname != null && !nickname.trim().isEmpty()) {
                    commentDTO.setNickname(nickname);
                } else {
                    // 닉네임이 없으면 이름 사용
                    String name = commentEntity.getMember().getMemberName();
                    commentDTO.setNickname(name != null && !name.trim().isEmpty() ? name : "익명");
                }
            } catch (Exception e) {
                // LazyInitializationException 등이 발생할 수 있으므로 이름으로 대체
                String name = commentEntity.getMember().getMemberName();
                commentDTO.setNickname(name != null && !name.trim().isEmpty() ? name : "익명");
            }
        } else if (commentEntity.getLawyer() != null) {
            try {
                String nickname = commentEntity.getLawyer().getLawyerNickname();
                if (nickname != null && !nickname.trim().isEmpty()) {
                    commentDTO.setNickname(nickname);
                } else {
                    // 닉네임이 없으면 이름 사용
                    String name = commentEntity.getLawyer().getLawyerName();
                    commentDTO.setNickname(name != null && !name.trim().isEmpty() ? name : "익명");
                }
            } catch (Exception e) {
                // LazyInitializationException 등이 발생할 수 있으므로 이름으로 대체
                String name = commentEntity.getLawyer().getLawyerName();
                commentDTO.setNickname(name != null && !name.trim().isEmpty() ? name : "익명");
            }
        } else {
            // member와 lawyer가 모두 null인 경우 - fetch join이 실패했을 가능성
            // memberIdx나 lawyerIdx를 직접 사용해서 수동으로 조회
            if (memberIdx != null) {
                try {
                    var member = memberRepository.findById(memberIdx).orElse(null);
                    if (member != null) {
                        String nickname = member.getMemberNickname();
                        String name = member.getMemberName();
                        commentDTO.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname : (name != null && !name.trim().isEmpty() ? name : "익명"));
                    } else {
                        commentDTO.setNickname("익명");
                    }
                } catch (Exception e) {
                    commentDTO.setNickname("익명");
                }
            } else if (lawyerIdx != null) {
                try {
                    var lawyer = lawyerRepository.findById(lawyerIdx).orElse(null);
                    if (lawyer != null) {
                        String nickname = lawyer.getLawyerNickname();
                        String name = lawyer.getLawyerName();
                        commentDTO.setNickname(nickname != null && !nickname.trim().isEmpty() ? nickname : (name != null && !name.trim().isEmpty() ? name : "익명"));
                    } else {
                        commentDTO.setNickname("익명");
                    }
                } catch (Exception e) {
                    commentDTO.setNickname("익명");
                }
            } else {
                commentDTO.setNickname("익명");
            }
        }
        
        return commentDTO;
    }

    // 댓글 작성 (일반 회원)
    @Transactional
    public CommentDTO create(Integer newsIdx, Integer memberIdx, String commentContent) {
        // 뉴스 글 존재 확인 및 공지사항 체크
        var newsBoard = newsBoardRepository.findById(newsIdx)
                .orElseThrow(() -> new DataNotFoundException("news board not found"));
        
        // 공지사항(categoryIdx = 1)은 댓글 불가
        if (newsBoard.getCategory() != null && newsBoard.getCategory().getCategoryIdx() == 1) {
            throw new IllegalStateException("공지사항에는 댓글을 작성할 수 없습니다.");
        }
        
        MemberEntity member = memberRepository.findById(memberIdx)
                .orElseThrow(() -> new DataNotFoundException("member not found"));
        
        CommentEntity comment = new CommentEntity();
        comment.setNewsIdx(newsIdx);
        comment.setMember(member);  // member를 설정하면 member_idx가 자동으로 설정됨
        comment.setCommentContent(commentContent);
        comment.setCommentRegDate(LocalDate.now());
        comment.setCommentActive(1); // 활성 상태
        
        CommentEntity savedComment = commentRepository.save(comment);
        // 저장 후 다시 조회하여 member와 lawyer를 fetch join으로 로드
        CommentEntity loadedComment = commentRepository.findByIdWithMemberAndLawyer(savedComment.getCommentIdx());
        return convertCommentDTO(loadedComment);
    }

    // 댓글 작성 (변호사)
    @Transactional
    public CommentDTO createByLawyer(Integer newsIdx, Integer lawyerIdx, String commentContent) {
        // 뉴스 글 존재 확인 및 공지사항 체크
        var newsBoard = newsBoardRepository.findById(newsIdx)
                .orElseThrow(() -> new DataNotFoundException("news board not found"));
        
        // 공지사항(categoryIdx = 1)은 댓글 불가
        if (newsBoard.getCategory() != null && newsBoard.getCategory().getCategoryIdx() == 1) {
            throw new IllegalStateException("공지사항에는 댓글을 작성할 수 없습니다.");
        }
        
        LawyerEntity lawyer = lawyerRepository.findById(lawyerIdx)
                .orElseThrow(() -> new DataNotFoundException("lawyer not found"));
        
        CommentEntity comment = new CommentEntity();
        comment.setNewsIdx(newsIdx);
        comment.setLawyer(lawyer);
        comment.setCommentContent(commentContent);
        comment.setCommentRegDate(LocalDate.now());
        comment.setCommentActive(1); // 활성 상태
        
        CommentEntity savedComment = commentRepository.save(comment);
        // 저장 후 다시 조회하여 member와 lawyer를 fetch join으로 로드
        CommentEntity loadedComment = commentRepository.findByIdWithMemberAndLawyer(savedComment.getCommentIdx());
        return convertCommentDTO(loadedComment);
    }

    // 댓글 목록 조회 (활성 댓글만)
    public List<CommentDTO> getCommentsByNewsIdx(Integer newsIdx) {
        List<CommentEntity> comments = commentRepository.findByNewsIdxAndCommentActiveOrderByCommentRegDateAsc(newsIdx, 1);
        return comments.stream()
                .map(this::convertCommentDTO)
                .collect(Collectors.toList());
    }

    // 댓글 단건 조회 (member와 lawyer를 fetch join으로 함께 조회)
    public CommentEntity getComment(Integer commentIdx) {
        CommentEntity comment = commentRepository.findByIdWithMemberAndLawyer(commentIdx);
        if (comment != null) {
            return comment;
        } else {
            throw new DataNotFoundException("comment not found");
        }
    }

    // 댓글 수정
    @Transactional
    public void modify(CommentEntity comment, String commentContent) {
        comment.setCommentContent(commentContent);
        commentRepository.save(comment);
    }

    // 댓글 소프트 삭제 (commentActive = 0)
    @Transactional
    public void delete(CommentEntity comment) {
        comment.setCommentActive(0);
        commentRepository.save(comment);
    }

    // 댓글 작성자 확인 (일반 회원)
    public boolean isCommentOwnerByMember(CommentEntity comment, Integer memberIdx) {
        return comment.getMember() != null && 
               comment.getMember().getMemberIdx() != null && 
               comment.getMember().getMemberIdx().equals(memberIdx);
    }

    // 댓글 작성자 확인 (변호사)
    public boolean isCommentOwnerByLawyer(CommentEntity comment, Integer lawyerIdx) {
        return comment.getLawyer() != null && 
               comment.getLawyer().getLawyerIdx() != null && 
               comment.getLawyer().getLawyerIdx().equals(lawyerIdx);
    }
}

