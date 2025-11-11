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
                "C:\\SOLDESK\\2nd-project\\YM\\lawlex_project\\src\\main\\java\\com\\soldesk\\team_project\\python\\%s",
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

    // orc 연동
    public Map<String, Object> runPythonOCR(String scriptName, String... args) {
        Map<String, Object> result = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try {
            String output = runPython(scriptName, args);
            String trimmed = output.trim();

            int startIdx = trimmed.indexOf('[');
            int endIdx = trimmed.lastIndexOf(']');

            // JSON인지 확인
            if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
                String jsonPart = trimmed.substring(startIdx, endIdx + 1);

                jsonPart = jsonPart.replace("'", "\"");

                List<String> texts = mapper.readValue(jsonPart, new TypeReference<List<String>>() {});
                result.put("valid", true);
                result.put("texts", texts);
            } else {
                result.put("valid", false);
                result.put("error", "OCR 실행 실패 또는 Traceback 발생: " + trimmed);
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.put("valid", false);
            result.put("error", "OCR 처리 중 예외 발생: " + e.getMessage());
        }

        return result;
    }
    
}