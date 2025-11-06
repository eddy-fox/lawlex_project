package com.soldesk.team_project.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.soldesk.team_project.dto.AdDTO;
import com.soldesk.team_project.dto.LawyerDTO;
import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.service.PurchaseService;
import com.soldesk.team_project.service.PythonService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("member")
@RequiredArgsConstructor
public class MemberController {

    private final PurchaseService purchaseService;
    private final PythonService pythonService;
    
    // 멤버 포인트
    @GetMapping("/point")
    public String pointMain(Model model) {
        
        int memberIdx = 1; // 회원정보 받아와서 넘겨야함
        
        // 포인트 구매 상품
        List<ProductDTO> productList = purchaseService.getBuyPointProduct();
        model.addAttribute("productList", productList);
        
        // 포인트 사용 내역 및 결제 내역
        List<PointDTO> pointList = purchaseService.getAllPoint(memberIdx);
        List<PurchaseDTO> purchaseList = purchaseService.getAllPurchase(memberIdx);

        model.addAttribute("pointList", pointList);
        model.addAttribute("purchaseList", purchaseList);
        
        return "member/point";
    }
    @PostMapping("/point")
    public String productPurchase(@RequestParam("selectedProduct") int productNum, Model model) {

        // 구매 검증을 위한 ID 생성
        String purchaseId = "order-" + System.currentTimeMillis();
        // 구매 요청 내역 생성
        PurchaseDTO purchase = purchaseService.createPendingPurchase(productNum, purchaseId); // 회원정보도 같이 줘야함
        model.addAttribute("purchase", purchase);
        
        return "payment/checkout"; // 넘어갈 때 회원 정보 같이 넘겨줘야함
    }

    @GetMapping("/testOCR") 
    public String testOCR(Model model) {

        LawyerDTO lawyerDTO = new LawyerDTO();
        lawyerDTO.setLawyerAuth(0);
        model.addAttribute("lawyerAuth", new LawyerDTO());

        return "member/testOCR";
    }
    @PostMapping("/verify-license")
    @ResponseBody
    public Map<String, Object> verifyLicense(
        @RequestParam("licenseNumber") String licenseNumber,
        @RequestParam("licenseImage") MultipartFile licenseImage) {

        Map<String, Object> result = new HashMap<>();     

        try {
            // 1. 업로드된 파일을 임시 경로에 저장
            File tempFile = File.createTempFile("license_", ".jpg");
            licenseImage.transferTo(tempFile);

            // 2. OCR 스크립트 실행
            Map<String, Object> ocrResult = pythonService.runPythonOCR("ocr.py", tempFile.toString());
            if (!(boolean) ocrResult.getOrDefault("valid", false)) {
                result.put("valid", false);
                result.put("error", ocrResult.get("error"));
                return result;
            }

            // 3. 출력값과 일치하는지 확인
            List<String> ocrTexts = (List<String>) ocrResult.get("texts");
            boolean matched = ocrTexts.stream().anyMatch(text -> text.contains(licenseNumber));

            // 4. 결과 전달, 임시파일 삭제
            result.put("valid", matched);
            result.put("message", matched ? "자격번호 일치!" : "자격번호 불일치!");
            result.put("ocrTexts", ocrTexts); // 테스트할때만 출력
            tempFile.delete();

        } catch (Exception e) {
            e.printStackTrace();
            result.put("valid", false);
            result.put("error", "검증 과정 중 오류 발생: " + e.getMessage());
        }

        return result;
    }

}
