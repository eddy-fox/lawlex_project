package com.soldesk.team_project.repository;

import com.soldesk.team_project.entity.UserMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMasterRepository extends JpaRepository<UserMasterEntity, Long> {
    boolean existsByUserId(String userId);
    Optional<UserMasterEntity> findByUserId(String userId);

    Optional<UserMasterEntity> findByMemberIdx(Integer memberIdx);
    Optional<UserMasterEntity> findByLawyerIdx(Integer lawyerIdx);
    Optional<UserMasterEntity> findByAdminIdx(Integer adminIdx);
}
