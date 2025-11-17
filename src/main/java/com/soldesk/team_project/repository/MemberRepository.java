package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer>{

    // 모든 일반 회원 조회
    List<MemberEntity> findByMemberActive(Integer memberActive);

    // 검색타입 별 일반 회원 검색
    List<MemberEntity> findByMemberIdxAndMemberActive(Integer memberIdx, Integer memberActive);
    List<MemberEntity> findByMemberIdContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(String memberId, Integer memberActive);
    List<MemberEntity> findByMemberNameContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(String memberName, Integer memberActive);
    List<MemberEntity> findByMemberIdnumContainingAndMemberActiveOrderByMemberIdnumAsc(String memberIdnum, Integer memberActive);
    List<MemberEntity> findByMemberEmailContainingIgnoreCaseAndMemberActiveOrderByMemberEmailAsc(String memberEmail, Integer memberActive);
    List<MemberEntity> findByMemberPhoneContainingAndMemberActiveOrderByMemberPhoneAsc(String memberPhone, Integer memberActive);
    List<MemberEntity> findByMemberNicknameContainingIgnoreCaseAndMemberActiveOrderByMemberNicknameAsc(String memberNickname, Integer memberActive);

    //
    Optional<MemberEntity> findByMemberId(String memberId);
    Optional<MemberEntity> findByMemberPhoneAndMemberIdnum(String memberPhone,String memberIdnum);
    Optional<MemberEntity> findByMemberIdAndMemberPhoneAndMemberIdnum(String memberId,String memberPhone,String memberIdnum);
    Optional<MemberEntity> findByMemberIdxAndMemberPhoneAndMemberIdnum(Integer memberIdx,String memberPhone,String memberIdnum);
    boolean existsByMemberId(String memberId);
    Optional<MemberEntity> findByMemberName(String memberName);

    // OAuth2 회원가입 중복체크
    Optional<MemberEntity> findByMemberProviderAndMemberProviderId(String memberProvider, String memberProviderId);


}