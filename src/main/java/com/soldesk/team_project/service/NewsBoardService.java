package com.soldesk.team_project.service;

import java.util.Date;
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
        boardDTO.setNews_idx(boardEntity.getNewsIdx());
        boardDTO.setNews_title(boardEntity.getNewsTitle());
        boardDTO.setNews_regDate(boardEntity.getNewsRegDate());
        boardDTO.setNews_imgPath(boardEntity.getNewsImgPath());
        boardDTO.setNews_views(boardEntity.getNewsViews());
        boardDTO.setLawyer_idx(boardEntity.getLawyer().getLawyerIdx());
        boardDTO.setCategory_idx(boardEntity.getCategory().getCategoryIdx());
        return boardDTO;
    }

    private NewsBoardEntity convertBoardEntity(NewsBoardDTO boardDTO){
        NewsBoardEntity boardEntity = new NewsBoardEntity();
        boardEntity.setNewsIdx(boardDTO.getNews_idx());
        boardEntity.setNewsTitle(boardDTO.getNews_title());
        boardEntity.setNewsRegDate(boardDTO.getNews_regDate());
        boardEntity.setNewsImgPath(boardDTO.getNews_imgPath());
        boardEntity.setNewsLike(boardDTO.getNews_like());
        
        LawyerEntity lawyerEntity = lawyerRepository.findById(boardDTO.getLawyer_idx()).orElse(null);
        boardEntity.setLawyer(lawyerEntity);
        NewsCategoryEntity categoryEntity = categoryRepository.findById(boardDTO.getCategory_idx()).orElse(null);
        boardEntity.setCategory(categoryEntity);

        return boardEntity;
    }

    private CategoryDTO convertCategoryDTO(NewsCategoryEntity categoryEntity){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategory_idx(categoryEntity.getCategoryIdx());
        categoryDTO.setCategory_name(categoryEntity.getCategoryName());
        return categoryDTO;
    }

    private NewsCategoryEntity convertCategoryEntity(CategoryDTO categoryDTO){
        NewsCategoryEntity categoryEntity = new NewsCategoryEntity();
        categoryEntity.setCategoryIdx(categoryDTO.getCategory_idx());
        categoryEntity.setCategoryName(categoryDTO.getCategory_name());
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
    public void deleteProcess(int news_idx){
        boardRepository.deleteById(news_idx);
    }

    @Transactional
    public void modifyProcess(NewsBoardDTO modifyBoard){
        NewsBoardEntity boardEntity = boardRepository.findById(modifyBoard.getNews_idx()).orElse(null);
        boardEntity.setNewsTitle(modifyBoard.getNews_title());
        boardEntity.setNewsContent(modifyBoard.getNews_content());
        boardRepository.save(boardEntity);
    }



    
}
