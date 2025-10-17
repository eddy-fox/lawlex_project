package com.soldeskp.p2.Service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.soldeskp.p2.Mapper.MemberMapper;
import com.soldeskp.p2.DTO.MemberDTO;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;

    public boolean joinMember(MemberDTO member) {
        if (member.getMemberAgree() == null) {
            member.setMemberAgree("N");
        }
        return memberMapper.insertMember(member) > 0;
    }

    // 아이디 중복확인
    public boolean checkDuplicateId(String memberId) {
        return memberMapper.checkId(memberId) > 0; // true면 이미 존재
    }
}

