package com.soldesk.team_project.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

@Service
public class PythonService {

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
    
}