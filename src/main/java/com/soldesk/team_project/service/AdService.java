package com.soldesk.team_project.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.entity.AdEntity;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.repository.AdRepository;
import com.soldesk.team_project.repository.LawyerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdService {
    
    private final AdRepository adRepository;
    private final LawyerRepository lawyerRepository;

    // Ad Entity -> DTO
    private AdDTO convertAdDTO (AdEntity adEntity) {
        AdDTO adDTO = new AdDTO();
        adDTO.setAdIdx(adEntity.getAdIdx());
        adDTO.setAdName(adEntity.getAdName());
        adDTO.setAdImgPath(adEntity.getAdImgPath());
        adDTO.setAdLink(adEntity.getAdLink());
        adDTO.setAdStartDate(adEntity.getAdStartDate());
        adDTO.setAdDuration(adEntity.getAdDuration());
        adDTO.setAdViews(adEntity.getAdViews());
        adDTO.setAdActive(adEntity.getAdActive());
        adDTO.setLawyerIdx(adEntity.getLawyerIdx());
        adDTO.setLawyerName(adEntity.getLawyer().getLawyerName());

        return adDTO;
    }

        // Ad DTO -> Entity
        private AdEntity convertAdEntity (AdDTO adDTO) {
            AdEntity adEntity = new AdEntity();
            adEntity.setAdIdx(adDTO.getAdIdx());
            adEntity.setAdName(adDTO.getAdName());
            adEntity.setAdImgPath(adDTO.getAdImgPath());
            adEntity.setAdLink(adDTO.getAdLink());
            adEntity.setAdStartDate(adDTO.getAdStartDate());
            adEntity.setAdDuration(adDTO.getAdDuration());
            adEntity.setAdViews(adDTO.getAdViews());
            adEntity.setAdActive(adDTO.getAdActive());
            adEntity.setLawyerIdx(adDTO.getLawyerIdx());

            LawyerEntity lawyerEntity = lawyerRepository.findById(adDTO.getLawyerIdx()).orElse(null);
            adEntity.setLawyer(lawyerEntity);

            return adEntity;
        }

    // 만료된 광고 비활성화
    @Transactional
    public void refreshActiveAds() {
        List<AdEntity> activeAds = adRepository.findByAdActive(1);
        LocalDate today = LocalDate.now();

        for (AdEntity ad : activeAds) {
            LocalDate startDate = ad.getAdStartDate();
            if (startDate == null) continue;
            int duration = ad.getAdDuration();
            LocalDate endDate = startDate.plusDays(duration);

            if (today.isAfter(endDate)) {
                ad.setAdActive(0);
                adRepository.save(ad);
            }
        }
    }
    
    // 활성 광고 조회
    public List<AdDTO> getAllAd() {
        List<AdEntity> adEntityList = adRepository.findByAdActiveOrderByAdIdxDesc(1);
        if (adEntityList == null) adEntityList = new ArrayList<>();
        
        return adEntityList.stream()
            .map(adEntity -> convertAdDTO(adEntity)).collect(Collectors.toList());
    }

    // 특정 광고 조회
    public AdDTO getAd(int adIdx) {
        AdEntity adEntity = adRepository.findById(adIdx).orElse(null);
        AdDTO adDTO = convertAdDTO(adEntity);
        return adDTO;
    }

    // 광고 등록
    @Transactional
    public void registProcess(AdDTO adRegistraion) {
        AdEntity adEntity = convertAdEntity(adRegistraion);
        // 광고 등록 시 active를 1로 설정
        adEntity.setAdActive(1);
        adEntity.setAdViews(0);
        adRepository.save(adEntity);
    }

    // 광고 수정
    @Transactional
    public void modifyProcess(AdDTO modifyAd) {
        AdEntity adEntity = adRepository.findById(modifyAd.getAdIdx()).orElse(null);
        adEntity.setAdName(modifyAd.getAdName());
        adEntity.setAdStartDate(modifyAd.getAdStartDate());
        adEntity.setAdDuration(modifyAd.getAdDuration());
        adEntity.setAdImgPath(modifyAd.getAdImgPath());
        adEntity.setAdLink(modifyAd.getAdLink());
        adRepository.save(adEntity);
    }

    // 광고 삭제
    @Transactional
    public void deleteProcess(int adIdx) {
        AdEntity adEntity = adRepository.findById(adIdx).orElse(null);
        adEntity.setAdActive(0);
        adRepository.save(adEntity);
    }

    // 광고 조회수 증가
    @Transactional
    public void increaseAdViews(int adIdx) {
        AdEntity adEntity = adRepository.findById(adIdx).orElse(null);
        int view = adEntity.getAdViews();
        view += 1;
        adEntity.setAdViews(view);
        adRepository.save(adEntity);
    }

}
