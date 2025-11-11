package com.soldesk.team_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="admin_idx")
    private Integer adminIdx;

    @Column(name="admin_id")
    private String adminId;

    @Column(name="admin_pass")
    private String adminPass;

    @Column(name="admin_name")
    private String adminName;

    @Column(name="admin_email")
    private String adminEmail;

    @Column(name="admin_phone")
    private String adminPhone;

    @Column(name="admin_role")
    private String adminRole;


}
