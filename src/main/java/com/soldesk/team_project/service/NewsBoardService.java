package com.soldesk.team_project.service;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.soldesk.team_project.dto.CategoryDTO;
import com.soldesk.team_project.dto.NewsBoardDTO;
import com.soldesk.team_project.entity.LawyerEntity;
import com.soldesk.team_project.entity.NewsBoardEntity;
import com.soldesk.team_project.entity.NewsCategoryEntity;
import com.soldesk.team_project.infra.DriveUploader;
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
    private final DriveUploader driveUploader;
    
    @Value("${google.drive.newsboard-folder-id}")
    private String newsFolderId;

    
    private NewsBoardDTO convertBoardDTO(NewsBoardEntity boardEntity){
        NewsBoardDTO boardDTO = new NewsBoardDTO();
        boardDTO.setNewsIdx(boardEntity.getNewsIdx());
        boardDTO.setNewsTitle(boardEntity.getNewsTitle());
        boardDTO.setNewsRegDate(boardEntity.getNewsRegDate());
        boardDTO.setNewsImgPath(boardEntity.getNewsImgPath());
        boardDTO.setNewsViews(boardEntity.getNewsViews());
        boardDTO.setLawyerIdx(boardEntity.getLawyer().getLawyerIdx());
        boardDTO.setCategoryIdx(boardEntity.getCategory().getCategoryIdx());
        if(boardEntity.getFileAttached() == 0) { 
            boardDTO.setFileAttached(boardEntity.getFileAttached());
        }else{
            boardDTO.setFileAttached(boardEntity.getFileAttached());
            boardDTO.setStoredFileName(boardEntity.getStoredFileName());
        }
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
    public void writeProcess(NewsBoardDTO writeBoard) throws Exception{
        if(writeBoard.getNewsBoardFile().isEmpty()||writeBoard.getNewsBoardFile()==null){
            writeBoard.setFileAttached(0);
            NewsBoardEntity boardEntity = convertBoardEntity(writeBoard);
            boardRepository.save(boardEntity);
        }else {
            MultipartFile newsboardFile = writeBoard.getNewsBoardFile();
            var info = driveUploader.upload(newsboardFile, newsFolderId);
            String savePath = info.directUrl(); //구글드라이브 폴더 경로입력
            writeBoard.setFileAttached(1);
            writeBoard.setNewsImgPath(savePath);
            writeBoard.setStoredFileName(info.name());
            NewsBoardEntity boardEntity = convertBoardEntity(writeBoard);
            boardRepository.save(boardEntity);
        }
    }
    

    public NewsBoardDTO getNewsBoard(int news_idx){
        NewsBoardEntity boardEntity = boardRepository.findById(news_idx).orElse(null);
        NewsBoardDTO boardDTO = convertBoardDTO(boardEntity);
        return boardDTO;
    }

    @Transactional
    public void modifyProcess(NewsBoardDTO modifyBoard) throws Exception{
        if(modifyBoard.getFileAttached() == 0){
        NewsBoardEntity boardEntity = boardRepository.findById(modifyBoard.getNewsIdx()).orElse(null);
        boardEntity.setNewsTitle(modifyBoard.getNewsTitle());
        boardEntity.setNewsContent(modifyBoard.getNewsContent());
        boardEntity.setNewsImgPath(modifyBoard.getNewsImgPath());
        boardRepository.save(boardEntity);
        }else{
            NewsBoardEntity boardEntity = boardRepository.findById(modifyBoard.getNewsIdx()).orElse(null);
            
            boardEntity.setNewsTitle(modifyBoard.getNewsTitle());
            boardEntity.setNewsContent(modifyBoard.getNewsContent());
            boardEntity.setNewsImgPath(modifyBoard.getNewsImgPath());
            MultipartFile newsboardFile = modifyBoard.getNewsBoardFile();
            var info = driveUploader.upload(newsboardFile, newsFolderId);
            String savePath = info.directUrl(); //구글드라이브 폴더 경로입력
            boardEntity.setFileAttached(1);
            boardEntity.setNewsImgPath(savePath);
            boardEntity.setStoredFileName(info.name());
            boardRepository.save(boardEntity);
        }
    }



    
}
