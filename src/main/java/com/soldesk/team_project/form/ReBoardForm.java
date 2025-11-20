package com.soldesk.team_project.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReBoardForm {
    
    private String reboardTitle; // 선택사항

    @NotEmpty(message = "내용은 필수 항목입니다.")
    private String reboardContent;
}
