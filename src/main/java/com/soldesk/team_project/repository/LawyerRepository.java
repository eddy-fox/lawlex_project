package com.soldesk.team_project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.LawyerEntity;

@Repository
public interface LawyerRepository extends JpaRepository<LawyerEntity, Integer>{

    List<LawyerEntity> findByLawyerIdx(Integer lawyerIdx);
    List<LawyerEntity> findByLawyerIdContainingIgnoreCaseOrderByLawyerIdAsc(String lawyerId);
    List<LawyerEntity> findByLawyerNameContainingIgnoreCaseOrderByLawyerIdAsc(String lawyerName);
    List<LawyerEntity> findByLawyerIdnumContainingOrderByLawyerIdnumAsc(String lawyerIdnum);
    List<LawyerEntity> findByLawyerEmailContainingIgnoreCaseOrderByLawyerEmailAsc(String lawyerEmail);
    List<LawyerEntity> findByLawyerPhoneContainingOrderByLawyerPhoneAsc(String lawyerPhone);
    List<LawyerEntity> findByLawyerNicknameContainingIgnoreCaseOrderByLawyerNicknameAsc(String lawyerNickname);
    List<LawyerEntity> findByLawyerAuthOrderByLawyerAuthAsc(Integer lawyerAuth);
    List<LawyerEntity> findByLawyerAddressContainingIgnoreCaseOrderByLawyerAddressAsc(String lawyerAddress);
    List<LawyerEntity> findByLawyerTelContainingOrderByLawyerTelAsc(String lawyerTel);
    List<LawyerEntity> findByLawyerCommentContainingIgnoreCaseOrderByLawyerCommentAsc(String lawyerComment);
    
} 

