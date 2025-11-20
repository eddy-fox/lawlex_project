package com.soldesk.team_project.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PythonService {

    // GPT 연동
    public String runPython(String scriptName, String... args) {
        try {
            // Python 스크립트 경로
            String scriptPath = String.format(
                "C:\\Users\\eddy_\\Documents\\2ndTeamProject\\team-project\\src\\main\\java\\com\\soldesk\\team_project\\python\\%s",
                scriptName
            );

            // 인자 합치기
            String joinedArgs = String.join("\" \"", args);
            String command = String.format("python \"%s\" \"%s\"", scriptPath, joinedArgs);

            // Python 프로세스 실행
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", command);
            pb.redirectErrorStream(true);
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = pb.start();

            // 결과 읽기
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            process.waitFor();
            return result.toString().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return "Python 실행 실패: " + e.getMessage();
        }
    }

    // OCR 연동
    public Map<String, Object> runPythonOCR(String scriptName, String... args) {
        Map<String, Object> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String output = runPython(scriptName, args);
            String trimmed = output.trim();

            // 여러 줄 출력에서 마지막 JSON 라인만 추출
            String jsonLine = extractLastJsonLine(trimmed);

            if (jsonLine != null && jsonLine.startsWith("[") && jsonLine.endsWith("]")) {
                // 직접 JSON 파싱
                List<String> texts = mapper.readValue(jsonLine, new TypeReference<List<String>>() {});
                result.put("valid", true);
                result.put("texts", texts);
            } else {
                result.put("valid", false);
                result.put("error", "OCR 실행 실패 또는 JSON 추출 실패: " + trimmed);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("valid", false);
            result.put("error", "OCR 처리 중 예외 발생: " + e.getMessage());
        }

        return result;
    }

    // Python 출력에서 마지막 JSON 배열 라인 추출
    private String extractLastJsonLine(String output) {
        if (output == null || output.isEmpty()) {
            return null;
        }

        // 줄바꿈으로 분리
        String[] lines = output.split("\n");

        // 뒤에서부터 찾기 (마지막 JSON이 실제 결과)
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                return line;
            }
        }

        return null;
    }
    
}