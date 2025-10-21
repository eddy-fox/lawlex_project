package com.soldesk.team_project.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminDTO {
    
    private Integer admin_idx;
    private String admin_id;
    private String admin_pass;
    private String admin_name;
    private String admin_email;
    private String admin_phone;
    private String admin_role;

}
