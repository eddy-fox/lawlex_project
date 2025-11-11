package com.soldesk.team_project.form;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReBoardForm {
    
    @NotEmpty(message = "내용은 필수 항목입니다.")
    @Size(max=200)
    private String reboardTitle;

    @NotEmpty(message = "내용은 필수 항목입니다.")
    private String reboardContent;
}
