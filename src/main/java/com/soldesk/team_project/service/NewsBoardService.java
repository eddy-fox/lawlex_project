package com.soldesk.team_project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soldesk.team_project.dto.CategoryDTO;
import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.entity.NewsCategoryEntity;
import com.soldesk.team_project.repository.LawyerRepository;
import com.soldesk.team_project.repository.NewsBoardRepository;
import com.soldesk.team_project.repository.NewsCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NewsBoardService {
    
    private final NewsCategoryRepository categoryRepository;
    private final NewsBoardRepository boardRepository;
    private final LawyerRepository lawyerRepository;

    
    private NewsBoardDTO convertBoardDTO(NewsBoardEntity boardEntity){
        NewsBoardDTO boardDTO = new NewsBoardDTO();
        boardDTO.setNewsIdx(boardEntity.getNewsIdx());
        boardDTO.setNewsTitle(boardEntity.getNewsTitle());
        boardDTO.setNewsRegDate(boardEntity.getNewsRegDate());
        boardDTO.setNewsImgPath(boardEntity.getNewsImgPath());
        boardDTO.setNewsViews(boardEntity.getNewsViews());
        boardDTO.setLawyerIdx(boardEntity.getLawyer().getLawyerIdx());
        boardDTO.setCategoryIdx(boardEntity.getCategory().getCategoryIdx());
        return boardDTO;
    }

    private NewsBoardEntity convertBoardEntity(NewsBoardDTO boardDTO){
        NewsBoardEntity boardEntity = new NewsBoardEntity();
        boardEntity.setNewsIdx(boardDTO.getNewsIdx());
        boardEntity.setNewsTitle(boardDTO.getNewsTitle());
        boardEntity.setNewsRegDate(boardDTO.getNewsRegDate());
        boardEntity.setNewsImgPath(boardDTO.getNewsImgPath());
        boardEntity.setNewsLike(boardDTO.getNewsLike());
        
        LawyerEntity lawyerEntity = lawyerRepository.findById(boardDTO.getLawyerIdx()).orElse(null);
        boardEntity.setLawyer(lawyerEntity);
        NewsCategoryEntity categoryEntity = categoryRepository.findById(boardDTO.getCategoryIdx()).orElse(null);
        boardEntity.setCategory(categoryEntity);

        return boardEntity;
    }

    private CategoryDTO convertCategoryDTO(NewsCategoryEntity categoryEntity){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryIdx(categoryEntity.getCategoryIdx());
        categoryDTO.setCategoryName(categoryEntity.getCategoryName());
        return categoryDTO;
    }

    private NewsCategoryEntity convertCategoryEntity(CategoryDTO categoryDTO){
        NewsCategoryEntity categoryEntity = new NewsCategoryEntity();
        categoryEntity.setCategoryIdx(categoryDTO.getCategoryIdx());
        categoryEntity.setCategoryName(categoryDTO.getCategoryName());
        return categoryEntity;
    }

    public List<NewsBoardDTO> getAllBoard(int category_idx){
        List<NewsBoardEntity> boardEntityList = boardRepository.findByCategoryCategoryIdxOrderByNewsIdxDesc(category_idx);
        return boardEntityList.stream().map(boardEntity -> convertBoardDTO(boardEntity)).collect(Collectors.toList());
    }

    public List<CategoryDTO> getAllCategory(int category_idx) { 
        List<NewsCategoryEntity> categoryEntityList = categoryRepository.findAll();
        return categoryEntityList.stream().map(categoryEntity -> convertCategoryDTO(categoryEntity)).collect(Collectors.toList());
    }

    public CategoryDTO getCategory(int category_idx){
        NewsCategoryEntity categoryEntity = categoryRepository.findById(category_idx).orElse(null);
        CategoryDTO categoryDTO = convertCategoryDTO(categoryEntity);
        return categoryDTO;
    }

    @Transactional
    public void writeProcess(NewsBoardDTO writeBoard){
        NewsBoardEntity boardEntity = convertBoardEntity(writeBoard);
        boardRepository.save(boardEntity);
    }

    public NewsBoardDTO getNewsBoard(int news_idx){
        NewsBoardEntity boardEntity = boardRepository.findById(news_idx).orElse(null);
        NewsBoardDTO boardDTO = convertBoardDTO(boardEntity);
        return boardDTO;
    }

    @Transactional
    public void modifyProcess(NewsBoardDTO modifyBoard){
        NewsBoardEntity boardEntity = boardRepository.findById(modifyBoard.getNewsIdx()).orElse(null);
        boardEntity.setNewsTitle(modifyBoard.getNewsTitle());
        boardEntity.setNewsContent(modifyBoard.getNewsContent());
        boardEntity.setNewsImgPath(modifyBoard.getNewsImgPath());
        boardRepository.save(boardEntity);
    }



    
}
