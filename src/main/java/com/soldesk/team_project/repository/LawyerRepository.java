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
    List<LawyerEntity> findByLawyerIdContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerId, Integer lawyerActive);
    List<LawyerEntity> findByLawyerNameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerName, Integer lawyerActive);
    List<LawyerEntity> findByLawyerIdnumContainingAndLawyerActiveOrderByLawyerIdxAsc(String lawyerIdnum, Integer lawyerActive);
    List<LawyerEntity> findByLawyerEmailContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerEmail, Integer lawyerActive);
    List<LawyerEntity> findByLawyerPhoneContainingAndLawyerActiveOrderByLawyerIdxAsc(String lawyerPhone, Integer lawyerActive);
    List<LawyerEntity> findByLawyerNicknameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerNickname, Integer lawyerActive);
    List<LawyerEntity> findByLawyerAuthAndLawyerActiveOrderByLawyerIdxAsc(Integer lawyerAuth, Integer lawyerActive);
    List<LawyerEntity> findByLawyerAddressContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerAddress, Integer lawyerActive);
    List<LawyerEntity> findByLawyerTelContainingAndLawyerActiveOrderByLawyerIdxAsc(String lawyerTel, Integer lawyerActive);
    List<LawyerEntity> findByLawyerCommentContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdxAsc(String lawyerComment, Integer lawyerActive);
    
    Optional<LawyerEntity> findByLawyerId(String lawyerId);
    Optional<LawyerEntity> findByLawyerName(String lawyerName);

    Optional<LawyerEntity> findByLawyerPhoneAndLawyerIdnum(String lawyerPhone, String lawyerIdnum);
    Optional<LawyerEntity> findByLawyerIdxAndLawyerPhoneAndLawyerIdnum(Integer lawyerIdx, String lawyerPhone, String lawyerIdnum);

    // OAuth2 회원가입 중복체크
    Optional<LawyerEntity> findByLawyerProviderAndLawyerProviderIdAndLawyerActive(String lawyerProvider, String lawyerProviderId, Integer lawyerActive);
    
    boolean existsByLawyerId(String lawyerId);

    // gpt 자동답변을 위한 idx검색
    LawyerEntity findByLawyerIdx(Integer lawyerIdx);

    // Optional<LawyerEntity> findByLawyerEmailAndLawyerActive(String email, Integer memberActive);

} 