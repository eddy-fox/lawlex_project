package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.DataNotFoundException;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.UserMasterEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.UserMasterRepository;
import com.soldesk.team_project.util.FileStorageService;

import lombok.RequiredArgsConstructor;

import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;


@Service
@RequiredArgsConstructor
public class LawyerService {
    
    private final LawyerRepository lawyerRepository;
    private final FileStorageService fileStorageService;
    private final PasswordEncoder passwordEncoder;

    private final UserMasterRepository userMasterRepository;
    private final InterestRepository interestRepository;
    
    private final MemberRepository memberRepository;


    private LawyerDTO convertLawyerDTO (LawyerEntity lawyerEntity) {
        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerIdx(lawyerEntity.getLawyerIdx());
        lawyerDTO.setLawyerId(lawyerEntity.getLawyerId());
        lawyerDTO.setLawyerPass(lawyerEntity.getLawyerPass());
        lawyerDTO.setLawyerName(lawyerEntity.getLawyerName());
        lawyerDTO.setLawyerIdnum(lawyerEntity.getLawyerIdnum());
        lawyerDTO.setLawyerEmail(lawyerEntity.getLawyerEmail());
        lawyerDTO.setLawyerPhone(lawyerEntity.getLawyerPhone());
        lawyerDTO.setLawyerAgree(lawyerEntity.getLawyerAgree());
        lawyerDTO.setLawyerNickname(lawyerEntity.getLawyerNickname());
        lawyerDTO.setLawyerAuth(lawyerEntity.getLawyerAuth());
        lawyerDTO.setLawyerAddress(lawyerEntity.getLawyerAddress());
        lawyerDTO.setLawyerTel(lawyerEntity.getLawyerTel());
        lawyerDTO.setLawyerImgPath(lawyerEntity.getLawyerImgPath());
        lawyerDTO.setLawyerComment(lawyerEntity.getLawyerComment());
        lawyerDTO.setLawyerLike(lawyerEntity.getLawyerLike());
        lawyerDTO.setLawyerAnswerCnt(lawyerEntity.getLawyerAnswerCnt());
        lawyerDTO.setLawyerActive(lawyerEntity.getLawyerActive());
        lawyerDTO.setInterestIdx(lawyerEntity.getInterestIdx());
        lawyerDTO.setInterestName(lawyerEntity.getInterest().getInterestName());

        return lawyerDTO;
    }

    // 전체 변호사 조회
    public List<LawyerDTO> getAllLawyer() {
        List<LawyerEntity> lawyerEntityList = lawyerRepository.findByLawyerActive(1);
        
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

    // 태그 별 특정 변호사 검색
    public List<LawyerDTO> searchLawyers(String searchType, String keyword) {
        List<LawyerEntity> lawyerEntityList;

        switch (searchType) {
            case "idx":
                try {
                    int idx = Integer.parseInt(keyword);
                    lawyerEntityList = lawyerRepository.findByLawyerIdxAndLawyerActive(idx, 1);
                } catch (NumberFormatException e) {
                    lawyerEntityList = new ArrayList<>();
                } break;
            case "id": lawyerEntityList = lawyerRepository.
                findByLawyerIdContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1); break;
            case "name": lawyerEntityList = lawyerRepository.
                findByLawyerNameContainingIgnoreCaseAndLawyerActiveOrderByLawyerIdAsc(keyword, 1); break;
            case "idnum": lawyerEntityList = lawyerRepository.
                findByLawyerIdnumContainingAndLawyerActiveOrderByLawyerIdnumAsc(keyword, 1); break;
            case "email": lawyerEntityList = lawyerRepository.
                findByLawyerEmailContainingIgnoreCaseAndLawyerActiveOrderByLawyerEmailAsc(keyword, 1); break;
            case "phone": lawyerEntityList = lawyerRepository.
                findByLawyerPhoneContainingAndLawyerActiveOrderByLawyerPhoneAsc(keyword, 1); break;
            case "nickname": lawyerEntityList = lawyerRepository.
                findByLawyerNicknameContainingIgnoreCaseAndLawyerActiveOrderByLawyerNicknameAsc(keyword, 1); break;
            case "auth": 
                try {
                    int auth = Integer.parseInt(keyword);
                    lawyerEntityList = lawyerRepository.
                        findByLawyerAuthAndLawyerActiveOrderByLawyerAuthAsc(auth, 1);
                } catch (NumberFormatException e) {
                    lawyerEntityList = new ArrayList<>();
                } break;
            case "address": 
                lawyerEntityList = lawyerRepository.
                    findByLawyerAddressContainingIgnoreCaseAndLawyerActiveOrderByLawyerAddressAsc(keyword, 1); 
                    break;
            case "tel": lawyerEntityList = lawyerRepository.
                findByLawyerTelContainingAndLawyerActiveOrderByLawyerTelAsc(keyword, 1); break;
            case "comment": lawyerEntityList = lawyerRepository.
                findByLawyerCommentContainingIgnoreCaseAndLawyerActiveOrderByLawyerCommentAsc(keyword, 1); break;
            
            default: lawyerEntityList = lawyerRepository.findByLawyerActive(1); break;
        }
        return lawyerEntityList.stream()
            .map(lawyerEntity -> convertLawyerDTO(lawyerEntity)).collect(Collectors.toList());
    }

    // 변호사 회원가입
    @Transactional
    public void joinFromPortal(LawyerDTO dto) {
        // 아이디 중복 (멤버/로이어 전체에서 중복 불가)
        if (isUserIdDuplicate(dto.getLawyerId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 비밀번호 암호화 (null 아니고 공백 아닐 때만)
        String encPass = null;
        if (notBlank(dto.getLawyerPass())) {
            encPass = passwordEncoder.encode(dto.getLawyerPass());
        } else {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        LawyerEntity le = LawyerEntity.builder()
                .lawyerId(dto.getLawyerId())
                .lawyerPass(encPass)
                .lawyerName(dto.getLawyerName())
                .lawyerEmail(dto.getLawyerEmail())
                .lawyerPhone(digits(dto.getLawyerPhone()))
                .lawyerTel(digits(dto.getLawyerTel()))
                .lawyerIdnum(digits(dto.getLawyerIdnum()))
                .lawyerAddress(dto.getLawyerAddress())
                .lawyerNickname(dto.getLawyerNickname())
                .interestIdx(dto.getInterestIdx())   // 로이어는 단일 관심사 사용
                .lawyerActive(1)                     // 활성
                .lawyerAuth(0)                       // 기본 미승인(필요시)
                .build();

        lawyerRepository.save(le);
    }

    // 변호사 프로필 수정 
    @Transactional
    public LawyerUpdateResult updateProfileFromPortal(
            LawyerDTO dto,
            String newPassword,
            String confirmPassword,
            Long /*unused*/ userIdx,
            Integer lawyerIdx
    ) {
        // userIdx는 현 흐름에서 사용하지 않음(로그인은 user_master 미사용)
        LawyerEntity le = lawyerRepository.findById(lawyerIdx)
                .orElseThrow(() -> new IllegalArgumentException("변호사 계정을 찾을 수 없습니다."));

        // 아이디 변경 처리
        if (notBlank(dto.getLawyerId()) && !dto.getLawyerId().equals(le.getLawyerId())) {
            if (isUserIdDuplicate(dto.getLawyerId())) {
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }
            le.setLawyerId(dto.getLawyerId());
        }

        // 비밀번호 변경
        if (notBlank(newPassword) || notBlank(confirmPassword)) {
            if (!notBlank(newPassword) || !newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            le.setLawyerPass(passwordEncoder.encode(newPassword));
        }

        // 기본 프로필 갱신
        if (notBlank(dto.getLawyerName()))     le.setLawyerName(dto.getLawyerName());
        if (notBlank(dto.getLawyerEmail()))    le.setLawyerEmail(dto.getLawyerEmail());
        if (notBlank(dto.getLawyerNickname())) le.setLawyerNickname(dto.getLawyerNickname());
        if (notBlank(dto.getLawyerAddress()))  le.setLawyerAddress(dto.getLawyerAddress());
        if (dto.getInterestIdx() != null)      le.setInterestIdx(dto.getInterestIdx());

        if (notBlank(dto.getLawyerPhone())) le.setLawyerPhone(digits(dto.getLawyerPhone()));
        if (notBlank(dto.getLawyerTel()))   le.setLawyerTel(digits(dto.getLawyerTel()));
        if (notBlank(dto.getLawyerIdnum())) le.setLawyerIdnum(digits(dto.getLawyerIdnum()));

        lawyerRepository.save(le);

        // 컨트롤러에서 세션 갱신에 쓰일 수 있도록 변경된 핵심 값 반환
        return new LawyerUpdateResult(le.getLawyerId(), le);
    }

    // 중복 체크 (멤버/로이어 통합)
    public boolean isUserIdDuplicate(String userId) {
        boolean memberDup = memberRepository.existsByMemberId(userId);
        boolean lawyerDup = lawyerRepository.existsByLawyerId(userId);
        return memberDup || lawyerDup;
    }

    
    // 유틸 
    private static String digits(String s) { return s == null ? null : s.replaceAll("\\D", ""); }
    private static boolean notBlank(String s) { return s != null && !s.isBlank(); }
    
    // 결과 DTO
    public record LawyerUpdateResult(String newUserId, LawyerEntity lawyer) {}
    
    // 로그인 아이디로 변호사 한 명 가져오기
    public LawyerEntity getLawyer(String lawyerId) {
        return lawyerRepository.findByLawyerId(lawyerId)
        .orElseThrow(() -> new DataNotFoundException("변호사를 찾을 수 없습니다."));
    }

    // 문의 상세 조회 필요한 id 와 name
    public LawyerDTO qLawyerInquiry(Integer lawyerIdx){
        LawyerEntity lawyerEntity = lawyerRepository.findById(lawyerIdx).orElse(null);
        LawyerDTO lawyerDTO = convertLawyerDTO(lawyerEntity);
        return lawyerDTO;
    }
}