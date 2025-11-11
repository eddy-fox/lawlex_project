package com.soldesk.team_project.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * lawyer_interest 테이블의 복합키를 표현하는 클래스
 * LawyerInterestEntity에서 @IdClass(...)로 사용함
 */
public class LawyerInterestId implements Serializable {

    private Integer lawyerIdx;
    private Integer interestIdx;

    // 기본 생성자 필수
    public LawyerInterestId() {
    }

    public LawyerInterestId(Integer lawyerIdx, Integer interestIdx) {
        this.lawyerIdx = lawyerIdx;
        this.interestIdx = interestIdx;
    }

    public Integer getLawyerIdx() {
        return lawyerIdx;
    }

    public void setLawyerIdx(Integer lawyerIdx) {
        this.lawyerIdx = lawyerIdx;
    }

    public Integer getInterestIdx() {
        return interestIdx;
    }

    public void setInterestIdx(Integer interestIdx) {
        this.interestIdx = interestIdx;
    }

    // 하이버네이트가 경고하던 그 부분: equals / hashCode 구현
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LawyerInterestId)) return false;
        LawyerInterestId that = (LawyerInterestId) o;
        return Objects.equals(lawyerIdx, that.lawyerIdx)
                && Objects.equals(interestIdx, that.interestIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lawyerIdx, interestIdx);
    }
}
