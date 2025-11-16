package com.soldesk.team_project.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CommentDTO {

    private Integer commentIdx;
    private String commentContent;
    private LocalDate commentRegDate;
    private Integer newsIdx;
    private Integer memberIdx;
    private Integer lawyerIdx;
    private Integer commentActive;
    private String nickname;  // 댓글 작성자 닉네임 (member 또는 lawyer에서 가져온 값)

}
