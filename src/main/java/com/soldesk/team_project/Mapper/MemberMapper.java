package com.soldeskp.p2.Mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.soldeskp.p2.DTO.MemberDTO;

@Mapper
public interface MemberMapper {

    // 기존 insert 유지
    @Insert("INSERT INTO member (member_id, member_pass, member_name, member_email, member_idnum, member_gender, member_address, member_nickname, member_phone, member_interest, member_agree) "
          + "VALUES (#{memberId}, #{memberPass}, #{memberName}, #{memberEmail}, #{memberIdnum}, #{memberGender}, #{memberAddress}, #{memberNickname}, #{memberPhone}, #{memberInterest}, #{memberAgree})")
    int insertMember(MemberDTO joinMember);

    // 아이디 중복확인
    @Select("SELECT COUNT(*) FROM member WHERE member_id = #{memberId}")
    int checkId(String memberId);
}
