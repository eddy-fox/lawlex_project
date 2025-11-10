package com.soldesk.team_project.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.InterestEntity;

import java.util.*;
public interface InterestRepository extends JpaRepository<InterestEntity,Integer> { 
    List<InterestEntity> findAllByOrderByInterestNameAsc(); 
}
