package com.soldesk.team_project.service;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.AdminDTO;
import com.soldesk.team_project.entity.AdminEntity;
import com.soldesk.team_project.repository.AdminRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    private AdminDTO convertAdminDTO (AdminEntity adminEntity) {
        AdminDTO adminDTO = new AdminDTO();
        adminDTO.setAdminIdx(adminEntity.getAdminIdx());
        adminDTO.setAdminId(adminEntity.getAdminId());
        adminDTO.setAdminPass(adminEntity.getAdminPass());
        adminDTO.setAdminName(adminEntity.getAdminName());
        adminDTO.setAdminEmail(adminEntity.getAdminEmail());
        adminDTO.setAdminPhone(adminEntity.getAdminPhone());
        adminDTO.setAdminRole(adminEntity.getAdminRole());

        return adminDTO;
    }

    public AdminDTO searchSessionAdmin(int adminIdx) {
        AdminEntity adminEntity = adminRepository.findById(adminIdx).orElse(null);
        AdminDTO adminDTO  = convertAdminDTO(adminEntity);

        return adminDTO;
    }
    
}