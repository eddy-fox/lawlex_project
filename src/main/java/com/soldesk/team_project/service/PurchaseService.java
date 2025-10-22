package com.soldesk.team_project.service;

import org.springframework.stereotype.Service;

import com.soldesk.team_project.dto.ProductDTO;
import com.soldesk.team_project.entity.ProductEntity;
import com.soldesk.team_project.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    
    private final ProductRepository productRepository;

    private ProductDTO convertProductDTO (ProductEntity productEntity) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductIdx(productEntity.getProductIdx());
        productDTO.setProductContent(productEntity.getProductContent());
        productDTO.setProductPrice(productEntity.getProductPrice());
        productDTO.setProductActive(productEntity.getProductActive());

        return productDTO;
    }

    public ProductDTO getProduct(int productNum) {
        ProductEntity productEntity = productRepository.findByProductIdxAndProductActive(productNum, 1);
        ProductDTO productDTO = convertProductDTO(productEntity);
        
        return productDTO;
    }

}
