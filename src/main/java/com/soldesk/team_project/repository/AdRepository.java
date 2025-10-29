package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.AdEntity;

@Repository
public interface AdRepository extends JpaRepository<AdEntity, Integer>{
    
    List<AdEntity> findByAdActive (Integer adActive);
    List<AdEntity> findByAdActiveOrderByAdIdxDesc (Integer adActive);
    
}
