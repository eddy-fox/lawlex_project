package com.soldesk.team_project.repository;

import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.NewsCategoryEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface NewsCategoryRepository extends JpaRepository<NewsCategoryEntity, Integer>{
    
    List<NewsCategoryEntity> findAll();

}
