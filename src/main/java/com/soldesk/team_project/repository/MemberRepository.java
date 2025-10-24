package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer>{

    List<MemberEntity> findByMemberActive(Integer memberActive);

    List<MemberEntity> findByMemberIdxAndMemberActive(Integer memberIdx, Integer memberActive);
    List<MemberEntity> findByMemberIdContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(String memberId, Integer memberActive);
    List<MemberEntity> findByMemberNameContainingIgnoreCaseAndMemberActiveOrderByMemberIdAsc(String memberName, Integer memberActive);
    List<MemberEntity> findByMemberIdnumContainingAndMemberActiveOrderByMemberIdnumAsc(String memberIdnum, Integer memberActive);
    List<MemberEntity> findByMemberEmailContainingIgnoreCaseAndMemberActiveOrderByMemberEmailAsc(String memberEmail, Integer memberActive);
    List<MemberEntity> findByMemberPhoneContainingAndMemberActiveOrderByMemberPhoneAsc(String memberPhone, Integer memberActive);
    List<MemberEntity> findByMemberNicknameContainingIgnoreCaseAndMemberActiveOrderByMemberNicknameAsc(String memberNickname, Integer memberActive);

    Optional<MemberEntity> findByMemberName(String memberName);

}