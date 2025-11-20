package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.PointDTO;
import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.dto.PurchaseDTO;
import com.soldesk.team_project.entity.MemberEntity;
import com.soldesk.team_project.entity.PointEntity;
import com.soldesk.team_project.entity.ProductEntity;
import com.soldesk.team_project.entity.PurchaseEntity;
import com.soldesk.team_project.repository.MemberRepository;
import com.soldesk.team_project.repository.PointRepository;
import com.soldesk.team_project.repository.ProductRepository;
import com.soldesk.team_project.repository.PurchaseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    private final ProductRepository productRepository;
    private final PurchaseRepository purchaseRepository;
    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    // point Entity -> DTO
    private PointDTO convertPointDTO (PointEntity pointEntity) {
        PointDTO pointDTO = new PointDTO();
        pointDTO.setPointIdx(pointEntity.getPointIdx());
        pointDTO.setPointDivision(pointEntity.getPointDivision());
        pointDTO.setPointState(pointEntity.getPointState());
        pointDTO.setPointHistory(pointEntity.getPointHistory());
        pointDTO.setPointRegDate(pointEntity.getPointRegDate());
        pointDTO.setMemberIdx(pointEntity.getMemberIdx());

        return pointDTO;
    }

    // product Entity -> DTO
    private ProductDTO convertProductDTO (ProductEntity productEntity) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductIdx(productEntity.getProductIdx());
        productDTO.setProductContent(productEntity.getProductContent());
        productDTO.setProductPrice(productEntity.getProductPrice());
        productDTO.setProductActive(productEntity.getProductActive());

        return productDTO;
    }

    // purchase Entity -> DTO
    private PurchaseDTO convertPurchaseDTO (PurchaseEntity purchaseEntity) {
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setPurchaseIdx(purchaseEntity.getPurchaseIdx());
        purchaseDTO.setPurchaseId(purchaseEntity.getPurchaseId());
        purchaseDTO.setPurchaseState(purchaseEntity.getPurchaseState());
        purchaseDTO.setPurchaseLegDate(purchaseEntity.getPurchaseLegDate());
        purchaseDTO.setProductIdx(purchaseEntity.getProductIdx());
        purchaseDTO.setMemberIdx(purchaseEntity.getMemberIdx());

        ProductEntity productEntity = purchaseEntity.getProduct();
        if (productEntity == null && purchaseEntity.getProductIdx() != null) {
            productEntity = productRepository.findById(purchaseEntity.getProductIdx()).orElse(null);
        }
        if (productEntity != null) {
            purchaseDTO.setProductPrice(productEntity.getProductPrice());
            purchaseDTO.setProductContent(productEntity.getProductContent());
        }

        return purchaseDTO;
    }

    // purchase DTO -> Entity
    private PurchaseEntity convertPurchaseEntity (PurchaseDTO purchaseDTO) {
        PurchaseEntity purchaseEntity = new PurchaseEntity();
        purchaseEntity.setPurchaseIdx(purchaseDTO.getPurchaseIdx());
        purchaseEntity.setPurchaseId(purchaseDTO.getPurchaseId());
        purchaseEntity.setPurchaseState(purchaseDTO.getPurchaseState());
        purchaseEntity.setPurchaseLegDate(purchaseDTO.getPurchaseLegDate());
        purchaseEntity.setProductIdx(purchaseDTO.getProductIdx());
        purchaseEntity.setMemberIdx(purchaseDTO.getMemberIdx());
    
        ProductEntity productEntity = productRepository.findById(purchaseDTO.getProductIdx()).orElse(null);
        MemberEntity memberEntity = memberRepository.findById(purchaseDTO.getMemberIdx()).orElse(null);
    
        purchaseEntity.setProduct(productEntity);
        purchaseEntity.setMember(memberEntity);

        return purchaseEntity;
    }

    // 포인트 구매 상품만 가져오기
    public List<ProductDTO> getBuyPointProduct() {
        List<ProductEntity> productEntityList = productRepository
            .findByProductContentContainingAndProductActiveOrderByProductIdxAsc("포인트", 1);

        return productEntityList.stream()
            .map(productEntity -> convertProductDTO(productEntity)).collect(Collectors.toList());
    }


    // 모든 포인트 내역 조회
    public List<PointDTO> getAllPoint(int memberIdx) {
        List<PointEntity> pointEntityList = pointRepository
            .findByMemberIdxOrderByPointIdxDesc(memberIdx);

        return pointEntityList.stream()
            .map(pointEntity -> convertPointDTO(pointEntity)).collect(Collectors.toList());
    }

    // 모든 구매 내역 조회
    public List<PurchaseDTO> getAllPurchase(int memberIdx) {
        List<PurchaseEntity> purchaseEntityList = purchaseRepository
            .findByMemberIdxAndPurchaseStateOrderByPurchaseIdxDesc(memberIdx, "success");

        return purchaseEntityList.stream()
            .map(purchaseEntity -> convertPurchaseDTO(purchaseEntity)).collect(Collectors.toList());
    }

    // 상품 조회
    public ProductDTO getProduct(int productNum) {
        ProductEntity productEntity = productRepository.findByProductIdxAndProductActive(productNum, 1);
        ProductDTO productDTO = convertProductDTO(productEntity);
        
        return productDTO;
    }

    // 주문 정보 생성
    public PurchaseDTO createPendingPurchase(int productIdx, String purchaseId, int memberIdx) {
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setProductIdx(productIdx);
        purchaseDTO.setMemberIdx(memberIdx);
        purchaseDTO.setPurchaseId(purchaseId);
        purchaseDTO.setPurchaseState("pending");

        PurchaseEntity purchaseEntity = convertPurchaseEntity(purchaseDTO);
        purchaseRepository.save(purchaseEntity);

        return purchaseDTO;
    }

    // 상품 가격 조회
    public String getProductPrice(int productNum) {
        ProductEntity productEntity = productRepository.findByProductIdxAndProductActive(productNum, 1);
        String productPrice = productEntity.getProductPrice();
        
        return productPrice;
    }

    // 주문 정보 조회
    public PurchaseDTO getOrderInfo(String purchaseId) {
        PurchaseEntity purchaseEntity = purchaseRepository.findByPurchaseId(purchaseId);
        PurchaseDTO purchaseDTO = convertPurchaseDTO(purchaseEntity);

        return purchaseDTO;
    }

    // 주문 상태 변경
    @Transactional
    public void updatePurchaseStatus(String purchaseId, String purchaseState) {
        PurchaseEntity purchaseEntity = purchaseRepository.findByPurchaseId(purchaseId);
        purchaseEntity.setPurchaseState(purchaseState);
        purchaseRepository.save(purchaseEntity);
    }

    // 회원 포인트 변동
    @Transactional
    public void purchasePoint(int memberIdx, int productIdx) {
        
        // 1. 상품 내용 조회
        ProductEntity productEntity = productRepository.findByProductIdxAndProductActive(productIdx, 1);
        String productContent = productEntity.getProductContent();
        String[] splitContent = productContent.trim().split(" ");
        int content = Integer.parseInt(splitContent[0]);

        // 2. 포인트 잔액 변동
        MemberEntity memberEntity = memberRepository.findById(memberIdx).orElse(null);
        int updatePoint = memberEntity.getMemberPoint() + content;
        memberEntity.setMemberPoint(updatePoint);

        // 3. 포인트 변동사항 적용
        PointEntity pointEntity = new PointEntity();
        pointEntity.setPointDivision("충전");
        pointEntity.setPointState(content);
        pointEntity.setPointHistory(content + " 포인트 충전");
        pointEntity.setMemberIdx(memberIdx);

        // 4. 저장
        memberRepository.save(memberEntity);
        pointRepository.save(pointEntity);
    }

    @Transactional
    public void usePoint(int memberIdx, int amount) {
        MemberEntity member = memberRepository.findById(memberIdx).orElseThrow();
        int cur = member.getMemberPoint();
        if (cur < amount) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        member.setMemberPoint(cur - amount);

        PointEntity point = new PointEntity();
        point.setPointDivision("사용");
        point.setPointState(-amount);
        point.setPointHistory(amount + " 포인트 상담 차감");
        point.setMemberIdx(memberIdx);

        memberRepository.save(member);
        pointRepository.save(point);
    }

}
