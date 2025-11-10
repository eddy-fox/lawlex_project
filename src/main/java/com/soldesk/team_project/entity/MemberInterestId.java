package com.soldesk.team_project.entity;

import java.io.Serializable;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class MemberInterestId implements Serializable {
    private Integer memberIdx;
    private Integer interestIdx;
}
