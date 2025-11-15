package com.soldesk.team_project.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CategoryRecommendService {

    @Value("${category.recommend.api.url:http://localhost:8000}")
    private String apiUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 제목을 기반으로 카테고리를 추천받습니다.
     * @param title 게시글 제목
     * @param topK 추천받을 카테고리 개수 (기본값: 5)
     * @return 추천된 카테고리 리스트 (점수 높은 순)
     */
    public List<String> recommendCategories(String title, int topK) {
        List<String> categories = new ArrayList<>();
        
        if (title == null || title.trim().isEmpty()) {
            return categories;
        }

        try {
            URL url = new URL(apiUrl + "/predict");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 생성
            String requestBody = String.format(
                "{\"title\": \"%s\", \"top_k\": %d}",
                escapeJson(title.trim()),
                topK
            );

            // 요청 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 읽기
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    // JSON 파싱
                    JsonNode jsonNode = objectMapper.readTree(response.toString());
                    JsonNode predictions = jsonNode.get("predictions");
                    
                    if (predictions != null && predictions.isArray()) {
                        for (JsonNode pred : predictions) {
                            String label = pred.get("label").asText();
                            categories.add(label);
                        }
                    }
                }
            } else {
                log.warn("카테고리 추천 API 호출 실패: HTTP {}", responseCode);
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    log.warn("에러 응답: {}", errorResponse.toString());
                }
            }

        } catch (java.net.ConnectException e) {
            log.warn("FastAPI 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요: {}", apiUrl);
            // 연결 실패 시 빈 리스트 반환 (애플리케이션은 계속 동작)
        } catch (Exception e) {
            log.error("카테고리 추천 서비스 오류: {}", e.getMessage(), e);
        }

        return categories;
    }

    /**
     * JSON 문자열 이스케이프
     */
    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}

