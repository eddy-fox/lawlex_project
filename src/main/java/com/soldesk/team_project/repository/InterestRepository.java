package com.soldesk.team_project.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.soldesk.team_project.entity.InterestEntity;

import java.util.*;
public interface InterestRepository extends JpaRepository<InterestEntity,Integer> { 
    Optional<InterestEntity> findByInterestName(String interestName);

    // 여러 개 이름으로 한 번에 가져오고 싶을 때 (옵션)
    List<InterestEntity> findByInterestNameIn(List<String> names);

    List<InterestEntity> findAllByOrderByInterestNameAsc();
}
