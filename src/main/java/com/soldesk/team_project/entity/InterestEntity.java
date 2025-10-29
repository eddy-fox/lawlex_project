package com.soldesk.team_project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interest")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InterestEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "interest_idx")
    private Integer interestIdx;

    @Column(name = "interest_name")
    private String interestName;
    
    @OneToMany(mappedBy = "interest")
    private java.util.List<MemberEntity> members;

    @OneToMany(mappedBy = "interest")
    private java.util.List<LawyerEntity> lawyers;
    
}
