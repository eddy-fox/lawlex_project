package com.soldesk.team_project.repository;

import java.util.Optional;
import com.soldesk.team_project.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends JpaRepository<AdminEntity, Integer> {
   
boolean existsByAdminId(String adminId);
Optional<AdminEntity> findByAdminId(String adminId);
}
