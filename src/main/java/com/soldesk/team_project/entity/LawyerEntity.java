package com.soldesk.team_project.entity;

import java.util.List;

import com.soldesk.team_project.security.UserBase;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder.Default;

@Getter
@Setter
@Builder
@Entity
@Table(name = "lawyer")
@NoArgsConstructor
@AllArgsConstructor
public class LawyerEntity implements UserBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lawyer_idx")
    private Integer lawyerIdx;

    @Column(name = "lawyer_id", nullable = false, unique = true, length = 255)
    private String lawyerId;

    @Column(name = "lawyer_pass", nullable = false, length = 255)
    private String lawyerPass;

    @Column(name = "lawyer_name")
    private String lawyerName;

    @Column(name = "lawyer_idnum")
    private String lawyerIdnum;

    @Column(name = "lawyer_email")
    private String lawyerEmail;

    // 휴대폰 번호
    @Column(name = "lawyer_phone")
    private String lawyerPhone;

    // 동의 여부 ("1" / "0" 등 문자열)
    @Column(name = "lawyer_agree")
    private String lawyerAgree;

    @Column(name = "lawyer_nickname")
    private String lawyerNickname;

    @Column(name = "lawyer_auth")
    private Integer lawyerAuth;

    // 사무실 주소
    @Column(name = "lawyer_address")
    private String lawyerAddress;

    // 사업장 전화번호
    @Column(name = "lawyer_tel")
    private String lawyerTel;

    @Column(name = "lawyer_img_path")
    private String lawyerImgPath;

    // 한 줄 소개
    @Column(name = "lawyer_comment")
    private String lawyerComment;

    @Column(name = "lawyer_provider")
    private String lawyerProvider; // ex: "google"

    @Column(name = "lawyer_provider_id")
    private String lawyerProviderId; // ex: 구글 sub 값
   
    @Default
    @Column(name = "lawyer_like", columnDefinition = "TINYINT(0) DEFAULT 0")
    private Integer lawyerLike = 0;

    @Default
    @Column(name = "lawyer_answer_cnt", columnDefinition = "TINYINT(0) DEFAULT 0")
    private Integer lawyerAnswerCnt = 0;

    @Default
    @Column(name = "lawyer_active", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Integer lawyerActive = 1;

    @Column(name = "interest_idx", nullable = false)
    private Integer interestIdx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_idx", insertable = false, updatable = false)
    private InterestEntity interest;

    @OneToMany(mappedBy = "lawyer")
    private List<AdEntity> ad;

    @OneToMany(mappedBy = "lawyer")
    private List<QuestionEntity> question;

    @OneToMany(mappedBy = "lawyer")
    private List<ReBoardEntity> reboard;

    // Oauth2User용 메서드
    @Override
    public Integer getIdx() {
        return this.lawyerIdx;
    }

    @Override
    public String getEmail() {
        return this.lawyerEmail;
    }

    @Override
    public String getName() {
        return this.lawyerName;
    }

    @Override
    public Integer isActive() {
        return this.lawyerActive;
    }

    @Override
    public String getUserType() {
        return "lawyer";
    }
}
