package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.LawyerEntity;

@Repository
public interface LawyerRepository extends JpaRepository<LawyerEntity, Integer>{

    List<LawyerEntity> findByLawyerIdx(Integer LawyerIdx);
    List<LawyerEntity> findByLawyerIdContainingIgnoreCaseOrderByLawyerIdAsc(String LawyerId);
    List<LawyerEntity> findByLawyerNameContainingIgnoreCaseOrderByLawyerIdAsc(String LawyerName);
    List<LawyerEntity> findByLawyerIdnumContainingOrderByLawyerIdnumAsc(String LawyerIdnum);
    List<LawyerEntity> findByLawyerEmailContainingIgnoreCaseOrderByLawyerEmailAsc(String LawyerEmail);
    List<LawyerEntity> findByLawyerPhoneContainingOrderByLawyerPhoneAsc(String LawyerPhone);
    List<LawyerEntity> findByLawyerNicknameContainingIgnoreCaseOrderByLawyerNicknameAsc(String LawyerNickname);
    List<LawyerEntity> findByLawyerAuthOrderByLawyerAuthAsc(Integer LawyerAuth);
    List<LawyerEntity> findByLawyerAddressContainingIgnoreCaseOrderByLawyerAddressAsc(String LawyerAddress);
    List<LawyerEntity> findByLawyerTelContainingOrderByLawyerTelAsc(String LawyerTel);
    List<LawyerEntity> findByLawyerCommentContainingIgnoreCaseOrderByLawyerCommentAsc(String LawyerComment);
    
} 

