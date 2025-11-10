package com.soldesk.team_project.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "user_master")
public class UserMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "user_id", nullable = false, unique = true, length = 100)
    private String userId;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "role", length = 20)   // "MEMBER" / "LAWYER" / "ADMIN" 
    private String role;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    //실제 값 보관
    @Column(name = "member_idx")
    private Integer memberIdx;

    @Column(name = "lawyer_idx")
    private Integer lawyerIdx;

    @Column(name = "admin_idx")
    private Integer adminIdx;

    // 읽기 전용 연관
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_idx", referencedColumnName = "member_idx",
                insertable = false, updatable = false)
    private MemberEntity member;   

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lawyer_idx", referencedColumnName = "lawyer_idx",
                insertable = false, updatable = false)
    private LawyerEntity lawyer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_idx", referencedColumnName = "admin_idx",
                insertable = false, updatable = false)
    private AdminEntity admin;
}
