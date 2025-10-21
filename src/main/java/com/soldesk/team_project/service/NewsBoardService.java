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
        boardDTO.setNews_idx(boardEntity.getNews_idx());
        boardDTO.setNews_title(boardEntity.getNews_title());
        boardDTO.setNews_regDate(boardEntity.getNews_regDate());
        boardDTO.setNews_imgPath(boardEntity.getNews_imgPath());
        boardDTO.setNews_views(boardEntity.getNews_views());
        boardDTO.setLawyer_idx(boardEntity.getLawyer().getLawyerIdx());
        boardDTO.setCategory_idx(boardEntity.getCategory().getCategory_idx());
        return boardDTO;
    }

    private NewsBoardEntity convertBoardEntity(NewsBoardDTO boardDTO){
        NewsBoardEntity boardEntity = new NewsBoardEntity();
        boardEntity.setNews_idx(boardDTO.getNews_idx());
        boardEntity.setNews_title(boardDTO.getNews_title());
        boardEntity.setNews_regDate(boardDTO.getNews_regDate());
        boardEntity.setNews_imgPath(boardDTO.getNews_imgPath());
        boardEntity.setNews_like(boardDTO.getNews_like());
        
        LawyerEntity lawyerEntity = lawyerRepository.findById(boardDTO.getLawyer_idx()).orElse(null);
        boardEntity.setLawyer(lawyerEntity);
        NewsCategoryEntity categoryEntity = categoryRepository.findById(boardDTO.getCategory_idx()).orElse(null);
        boardEntity.setCategory(categoryEntity);

        return boardEntity;
    }

    private CategoryDTO convertCategoryDTO(NewsCategoryEntity categoryEntity){
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategory_idx(categoryEntity.getCategory_idx());
        categoryDTO.setCategory_name(categoryEntity.getCategory_name());
        return categoryDTO;
    }

    private NewsCategoryEntity convertCategoryEntity(CategoryDTO categoryDTO){
        NewsCategoryEntity categoryEntity = new NewsCategoryEntity();
        categoryEntity.setCategory_idx(categoryDTO.getCategory_idx());
        categoryEntity.setCategory_name(categoryDTO.getCategory_name());
        return categoryEntity;
    }

    public List<NewsBoardDTO> getAllBoard(int category_idx){
        List<NewsBoardEntity> boardEntityList = boardRepository.findByCategoryIdxOrderByNewsIdxDesc(category_idx);
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
        boardEntity.setNews_title(modifyBoard.getNews_title());
        boardEntity.setNews_content(modifyBoard.getNews_content());
        boardRepository.save(boardEntity);
    }



    
}
