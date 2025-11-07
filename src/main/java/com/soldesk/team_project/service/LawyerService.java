package com.soldesk.team_project.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.LawyerRepository;
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


    @Transactional
    public LawyerEntity register(LawyerDTO dto) throws IOException {
        String idImg   = fileStorageService.saveLawyerFile(dto.getIdImage());
        String certImg = fileStorageService.saveLawyerFile(dto.getCertImage());

        LawyerEntity entity = LawyerEntity.builder()
                .lawyerId(dto.getLawyerId())
                .lawyerPass(passwordEncoder.encode(dto.getLawyerPass()))
                .lawyerName(dto.getLawyerName())
                .lawyerEmail(dto.getLawyerEmail())
                .lawyerIdnum(dto.getLawyerIdnum())
                .lawyerPhone(dto.getLawyerPhone())
                .lawyerNickname(dto.getLawyerNickname())
                .lawyerAgree(dto.getLawyerAgree())
                .lawyerAuth(0)
                .lawyerAddress(dto.getLawyerAddress())
                .lawyerTel(dto.getLawyerTel())
                .lawyerComment(dto.getLawyerComment())

                // 관심분야 
                .interestIdx(dto.getInterestIdx1())


                // 이미지: 모든 변형 컬럼에 함께 기록
                .lawyerImgPath(certImg)

                // 카운트 컬럼 초기값
                .lawyerLike(0)
                .lawyerAnswerCnt(0)

                .lawyerActive(1)
                .build();

        return lawyerRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<LawyerEntity> getPending() {
        return lawyerRepository.findByLawyerAuth(0);
    }

    @Transactional public void approve(Integer idx){
        lawyerRepository.findById(idx).ifPresent(e -> e.setLawyerAuth(1));
    }
    @Transactional public void reject(Integer idx){
        lawyerRepository.findById(idx).ifPresent(e -> e.setLawyerAuth(2));
    }

    @Transactional
    public void updateProfile(Integer idx, LawyerDTO dto) {
        LawyerEntity e = lawyerRepository.findById(idx).orElseThrow();
        e.setLawyerAddress(dto.getLawyerAddress());
        e.setLawyerTel(dto.getLawyerTel());
        e.setLawyerComment(dto.getLawyerComment());
        // 관심분야 수정이 필요하면 아래도 허용
        // e.setInterestIdx1(dto.getInterestIdx1());
        // e.setInterestIdx2(dto.getInterestIdx2());
        // e.setInterestIdx3(dto.getInterestIdx3());
    }
}
