package com.soldesk.team_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    private Integer admin_idx;

    @Column(name="admin_id")
    private String admin_id;

    @Column(name="admin_pass")
    private String admin_pass;

    @Column(name="admin_name")
    private String admin_name;

    @Column(name="admin_email")
    private String admin_email;

    @Column(name="admin_phone")
    private String admin_phone;

    @Column(name="admin_role")
    private String admin_role;
}
