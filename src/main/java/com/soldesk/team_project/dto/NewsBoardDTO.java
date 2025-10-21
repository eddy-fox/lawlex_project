package com.soldesk.team_project.dto;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewsBoardDTO {
    
    private Integer news_idx;
    private String news_title;
    private String news_content;
    private Date news_regDate;
    private String news_imgPath;
    private Integer news_like;
    private Integer news_views;
    
    private Integer admin_idx;
    private Integer lawyer_idx;
    private Integer category_idx; 
}
