package com.soldeskp.p2.DTO;

import lombok.Data;

@Data
public class MemberDTO {

    private int memberIdx;         // 회원 고유번호 (PK)
    private String memberId;       // 아이디
    private String memberPass;     // 비밀번호
    private String memberName;     // 이름
    private String memberEmail;    // 이메일
    private String memberIdnum;    // 주민번호 앞자리
    private String memberGender;   // 성별
    private String memberAddress;  // 주소
    private String memberNickname; // 닉네임
    private String memberPhone;    // 전화번호
    private String memberInterest; // 관심사
    private String memberAgree;    // 알림수신동의 여부 (Y/N)
}
