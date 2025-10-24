package com.soldesk.team_project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.soldesk.team_project.entity.MemberEntity;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Integer>{

    List<MemberEntity> findByMemberIdx(Integer memberIdx);
    List<MemberEntity> findByMemberIdContainingIgnoreCaseOrderByMemberIdAsc(String memberId);
    List<MemberEntity> findByMemberNameContainingIgnoreCaseOrderByMemberIdAsc(String memberName);
    List<MemberEntity> findByMemberIdnumContainingOrderByMemberIdnumAsc(String memberIdnum);
    List<MemberEntity> findByMemberEmailContainingIgnoreCaseOrderByMemberEmailAsc(String memberEmail);
    List<MemberEntity> findByMemberPhoneContainingOrderByMemberPhoneAsc(String memberPhone);
    List<MemberEntity> findByMemberNicknameContainingIgnoreCaseOrderByMemberNicknameAsc(String memberNickname);

    Optional<MemberEntity> findByMemberName(String memberName);

}