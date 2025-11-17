package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.LawyerEntity;

@Repository
public interface LawyerRepository extends JpaRepository<LawyerEntity, Integer>{

    // 모든 변호사 회원 조회
    List<LawyerEntity> findByLawyerActive(Integer lawyerActive);

    // lawyer_auth 값으로 목록 조회 (0:대기, 1:승인, 2:반려 등)
    List<LawyerEntity> findByLawyerAuth(Integer lawyerAuth);

    // 검색타입 별 변호사 회원 검색
    List<LawyerEntity> findByLawyerIdxAndLawyerActive(Integer lawyerIdx, Integer lawyerActive);
    List<LawyerEntity> findByLawyerIdContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(String lawyerId, Integer lawyerActive);
    List<LawyerEntity> findByLawyerNameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(String lawyerName, Integer lawyerActive);
    List<LawyerEntity> findByLawyerIdnumContainingAndLawyerActiveOrderByLawyerIdnumAsc(String lawyerIdnum, Integer lawyerActive);
    List<LawyerEntity> findByLawyerEmailContainingIgnoreCaseAndLawyerActiveOrderByLawyerEmailAsc(String lawyerEmail, Integer lawyerActive);
    List<LawyerEntity> findByLawyerPhoneContainingAndLawyerActiveOrderByLawyerPhoneAsc(String lawyerPhone, Integer lawyerActive);
    List<LawyerEntity> findByLawyerNicknameContainingIgnoreCaseAndLawyerActiveOrderByLawyerNicknameAsc(String lawyerNickname, Integer lawyerActive);
    List<LawyerEntity> findByLawyerAuthAndLawyerActiveOrderByLawyerAuthAsc(Integer lawyerAuth, Integer lawyerActive);
    List<LawyerEntity> findByLawyerAddressContainingIgnoreCaseAndLawyerActiveOrderByLawyerAddressAsc(String lawyerAddress, Integer lawyerActive);
    List<LawyerEntity> findByLawyerTelContainingAndLawyerActiveOrderByLawyerTelAsc(String lawyerTel, Integer lawyerActive);
    List<LawyerEntity> findByLawyerCommentContainingIgnoreCaseAndLawyerActiveOrderByLawyerCommentAsc(String lawyerComment, Integer lawyerActive);
    
    Optional<LawyerEntity> findByLawyerId(String lawyerId);
    Optional<LawyerEntity> findByLawyerName(String lawyerName);

    Optional<LawyerEntity> findByLawyerPhoneAndLawyerIdnum(String lawyerPhone, String lawyerIdnum);
    Optional<LawyerEntity> findByLawyerIdxAndLawyerPhoneAndLawyerIdnum(Integer lawyerIdx, String lawyerPhone, String lawyerIdnum);

    // OAuth2 회원가입 email 중복체크
    Optional<LawyerEntity> findByLawyerEmailAndLawyerActive(String email, Integer lawyerActive);
    
    boolean existsByLawyerId(String lawyerId);

    // Optional<LawyerEntity> findByLawyerEmailAndLawyerActive(String email, Integer memberActive);

} 