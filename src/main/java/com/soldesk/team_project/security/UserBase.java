package com.soldesk.team_project.security;

public interface UserBase {
    Integer getIdx();
    String getEmail();
    String getName();
    Integer isActive();
    String getUserType();
}
