package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.UserMasterEntity;
import com.soldesk.team_project.repository.InterestRepository;
import com.soldesk.team_project.repository.LawyerRepository;
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

    private static String trim(String s){ return s == null ? null : s.trim(); }
    private static String digits(String s){ return s == null ? null : s.replaceAll("\\D", ""); }
    private static boolean notBlank(String s){ return s != null && !s.isBlank(); }

    @Transactional
    public void joinFromPortal(LawyerDTO dto){
        if (userMasterRepository.existsByUserId(dto.getLawyerId())){
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        String enc = passwordEncoder.encode(dto.getLawyerPass());

        LawyerEntity le = LawyerEntity.builder()
                .lawyerId(dto.getLawyerId())
                .lawyerPass(enc)
                .lawyerName(dto.getLawyerName())
                .lawyerEmail(dto.getLawyerEmail())
                .lawyerPhone(dto.getLawyerPhone()!=null ? dto.getLawyerPhone().replaceAll("\\D","") : null)
                .lawyerIdnum(dto.getLawyerIdnum()!=null ? dto.getLawyerIdnum().replaceAll("\\D","") : null)
                .lawyerAddress(dto.getLawyerAddress())
                .lawyerComment(dto.getLawyerComment())
                .lawyerActive(1)
                .build();

        if (dto.getInterestIdx()!=null){
            // 읽기전용 컬럼(interest_idx)은 조인으로 세팅
            interestRepository.findById(dto.getInterestIdx()).ifPresent(le::setInterest);
            // 세션 즉시 반영 용도로 필드도 세팅(컬럼 insertable/updatable=false여도 세션 객체엔 값 보관 가능)
            le.setInterestIdx(dto.getInterestIdx());
        }

        le = lawyerRepository.save(le);

        UserMasterEntity u = UserMasterEntity.builder()
                .userId(dto.getLawyerId())
                .password(enc)
                .status("ACTIVE")
                .lawyerIdx(le.getLawyerIdx())
                .role("LAWYER")
                .build();
        userMasterRepository.save(u);
    }

    // 변호사 프로필 수정 (아이디/비번/이메일/주소/한줄소개/관심1)
    @Transactional
    public LawyerUpdateResult updateProfileFromPortal(LawyerDTO dto,
                                                      String newPassword,
                                                      String confirmPassword,
                                                      Long userIdx,
                                                      Integer lawyerIdx){
        UserMasterEntity u = userMasterRepository.findById(userIdx).orElseThrow();
        LawyerEntity le = lawyerRepository.findById(lawyerIdx).orElseThrow();

        // 아이디 변경
        if (dto.getLawyerId()!=null && !dto.getLawyerId().isBlank() && !dto.getLawyerId().equals(u.getUserId())){
            if (userMasterRepository.existsByUserId(dto.getLawyerId())){
                throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
            }
            u.setUserId(dto.getLawyerId());
            le.setLawyerId(dto.getLawyerId());
        }

        // 비밀번호 변경
        if ((newPassword!=null && !newPassword.isBlank()) || (confirmPassword!=null && !confirmPassword.isBlank())){
            if (newPassword==null || !newPassword.equals(confirmPassword)){
                throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
            }
            String enc = passwordEncoder.encode(newPassword);
            u.setPassword(enc);
            le.setLawyerPass(enc);
        }

        // 이메일/주소/소개
        le.setLawyerEmail(dto.getLawyerEmail());
        le.setLawyerAddress(dto.getLawyerAddress());
        le.setLawyerComment(dto.getLawyerComment());

        // 관심1 
        if (dto.getInterestIdx()!=null){
            interestRepository.findById(dto.getInterestIdx()).ifPresent(le::setInterest);
            le.setInterestIdx(dto.getInterestIdx());
        }

        userMasterRepository.save(u);
        lawyerRepository.save(le);

        return new LawyerUpdateResult(u.getUserId(), le);
    }

    public record LawyerUpdateResult(String newUserId, LawyerEntity lawyer) {

    }

    // 문의 상세 조회 필요한 id 와 name
    public LawyerDTO qLawyerInquiry(Integer lawyerIdx){
        LawyerEntity lawyerEntity = lawyerRepository.findById(lawyerIdx).orElse(null);
        LawyerDTO lawyerDTO = convertLawyerDTO(lawyerEntity);
        return lawyerDTO;
    }
    
}


