package com.soldesk.team_project.service;

import java.io.File;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FastApiServerService implements ApplicationRunner {

    @Value("${category.recommend.api.url:http://localhost:8000}")
    private String apiUrl;

    @Value("${fastapi.server.auto-start:true}")
    private boolean autoStart;

    @Value("${fastapi.server.python-command:python}")
    private String pythonCommand;

    @Value("${fastapi.server.auto-install:false}")
    private boolean autoInstall;

    private Process fastApiProcess;
    private static final String SERVER_SCRIPT_PATH = 
        "src/main/java/com/soldesk/team_project/python/model/server.py";

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!autoStart) {
            log.info("FastAPI 서버 자동 시작이 비활성화되어 있습니다.");
            return;
        }

        // 서버가 이미 실행 중인지 확인
        if (isServerRunning()) {
            log.info("FastAPI 서버가 이미 실행 중입니다.");
            return;
        }

        startFastApiServer();
    }

    /**
     * FastAPI 서버 시작
     */
    private void startFastApiServer() {
        try {
            // 프로젝트 루트 디렉토리 찾기
            String projectRoot = findProjectRoot();
            if (projectRoot == null) {
                log.error("프로젝트 루트 디렉토리를 찾을 수 없습니다.");
                return;
            }

            String scriptPath = Paths.get(projectRoot, SERVER_SCRIPT_PATH).toString();
            File scriptFile = new File(scriptPath);

            if (!scriptFile.exists()) {
                log.error("FastAPI 서버 스크립트를 찾을 수 없습니다: {}", scriptPath);
                return;
            }

            // requirements.txt 경로 확인
            String requirementsPath = Paths.get(scriptFile.getParent(), "requirements.txt").toString();
            File requirementsFile = new File(requirementsPath);
            
            // uvicorn 설치 확인
            if (!isUvicornInstalled()) {
                if (autoInstall) {
                    log.warn("uvicorn이 설치되지 않았습니다. 패키지 설치를 시도합니다.");
                    
                    // 먼저 최소 패키지만 설치 시도 (빠르고 안정적)
                    boolean installed = installMinimalPackages();
                    
                    // 최소 패키지 설치 실패 시 requirements.txt 시도
                    if (!installed && requirementsFile.exists()) {
                        log.warn("최소 패키지 설치 실패. requirements.txt를 사용하여 설치를 시도합니다.");
                        installed = installRequirements(requirementsFile);
                    }
                    
                    if (!installed) {
                        log.error("패키지 설치에 실패했습니다.");
                        log.error("수동으로 다음 명령어를 실행하세요: pip install uvicorn fastapi");
                        return;
                    }
                    
                    // 설치 후 다시 확인 (최대 5초 대기)
                    for (int i = 0; i < 5; i++) {
                        Thread.sleep(1000);
                        if (isUvicornInstalled()) {
                            break;
                        }
                        if (i == 4) {
                            log.error("패키지 설치 후에도 uvicorn을 찾을 수 없습니다.");
                            log.error("수동으로 다음 명령어를 실행하세요: pip install uvicorn fastapi");
                            return;
                        }
                    }
                } else {
                    log.error("uvicorn이 설치되지 않았습니다.");
                    log.error("다음 명령어를 실행하여 수동으로 설치하세요:");
                    log.error("  cd src/main/java/com/soldesk/team_project/python/model");
                    log.error("  pip install -r requirements.txt");
                    log.error("또는 최소 패키지만: pip install uvicorn[standard] fastapi");
                    log.error("자동 설치를 활성화하려면 application.properties에서 fastapi.server.auto-install=true로 설정하세요.");
                    return;
                }
            }

            // Python 서버 실행
            ProcessBuilder pb = createPythonProcessBuilder(
                "-m", "uvicorn",
                "server:app",
                "--host", "0.0.0.0",
                "--port", "8000"
            );

            // 작업 디렉토리를 스크립트가 있는 폴더로 설정
            pb.directory(new File(scriptFile.getParent()));
            pb.redirectErrorStream(false); // 에러 스트림을 별도로 읽기 위해 false
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            pb.environment().put("PYTHONUNBUFFERED", "1");

            fastApiProcess = pb.start();

            // 표준 출력 읽기 (별도 스레드)
            Thread outputReader = new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(fastApiProcess.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.debug("FastAPI 서버 출력: {}", line);
                    }
                } catch (Exception e) {
                    // 스트림이 닫혔을 때 정상 종료
                }
            });
            outputReader.setDaemon(true);
            outputReader.start();

            // 에러 스트림 읽기 (별도 스레드)
            Thread errorReader = new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(fastApiProcess.getErrorStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        log.warn("FastAPI 서버 에러: {}", line);
                    }
                } catch (Exception e) {
                    // 스트림이 닫혔을 때 정상 종료
                }
            });
            errorReader.setDaemon(true);
            errorReader.start();

            // 서버가 시작될 때까지 대기 (최대 10초)
            boolean serverStarted = false;
            for (int i = 0; i < 10; i++) {
                Thread.sleep(1000);
                if (fastApiProcess.isAlive() && isServerRunning()) {
                    serverStarted = true;
                    break;
                }
                if (!fastApiProcess.isAlive()) {
                    log.error("FastAPI 서버 프로세스가 종료되었습니다. 종료 코드: {}", fastApiProcess.exitValue());
                    break;
                }
            }

            if (serverStarted) {
                log.info("FastAPI 서버가 시작되었습니다. PID: {}", fastApiProcess.pid());
                log.info("서버 URL: {}", apiUrl);
            } else {
                log.error("FastAPI 서버 시작 실패. 서버가 응답하지 않습니다.");
                if (fastApiProcess != null && !fastApiProcess.isAlive()) {
                    log.error("프로세스 종료 코드: {}", fastApiProcess.exitValue());
                }
            }

        } catch (Exception e) {
            log.error("FastAPI 서버 시작 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 서버가 실행 중인지 확인
     */
    private boolean isServerRunning() {
        try {
            java.net.URL url = new java.net.URL(apiUrl + "/docs");
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.setReadTimeout(1000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            return responseCode == 200;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * uvicorn이 설치되어 있는지 확인
     */
    private boolean isUvicornInstalled() {
        try {
            ProcessBuilder pb = createPythonProcessBuilder("-m", "uvicorn", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Python 명령어를 배열로 분리하여 ProcessBuilder 생성
     */
    private ProcessBuilder createPythonProcessBuilder(String... args) {
        // pythonCommand에 공백이 있으면 (예: "py -3.11") 분리
        String[] commandParts = pythonCommand.split("\\s+");
        String[] fullCommand = new String[commandParts.length + args.length];
        System.arraycopy(commandParts, 0, fullCommand, 0, commandParts.length);
        System.arraycopy(args, 0, fullCommand, commandParts.length, args.length);
        return new ProcessBuilder(fullCommand);
    }

    /**
     * 최소 패키지만 설치 (uvicorn, fastapi)
     * @return 설치 성공 여부
     */
    private boolean installMinimalPackages() {
        try {
            log.info("최소 패키지(uvicorn, fastapi)를 설치합니다...");
            ProcessBuilder pb = createPythonProcessBuilder(
                "-m", "pip",
                "install", "uvicorn[standard]", "fastapi"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 출력 읽기 (간단히)
            Thread installReader = new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Successfully") || line.contains("ERROR") || line.contains("WARNING")) {
                            log.info("pip 설치: {}", line);
                        }
                    }
                } catch (Exception e) {
                    // 스트림이 닫혔을 때 정상 종료
                }
            });
            installReader.setDaemon(true);
            installReader.start();
            
            int exitCode = process.waitFor();
            installReader.join(1000);
            
            if (exitCode == 0) {
                log.info("최소 패키지 설치가 완료되었습니다.");
                return true;
            } else {
                log.warn("최소 패키지 설치 실패. 종료 코드: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("최소 패키지 설치 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }

    /**
     * requirements.txt를 사용하여 패키지 설치
     * @return 설치 성공 여부
     */
    private boolean installRequirements(File requirementsFile) {
        try {
            log.info("requirements.txt를 사용하여 패키지를 설치합니다: {}", requirementsFile.getAbsolutePath());
            log.warn("패키지 설치에는 시간이 걸릴 수 있습니다. 잠시만 기다려주세요...");
            
            ProcessBuilder pb = createPythonProcessBuilder(
                "-m", "pip",
                "install", "-r", requirementsFile.getAbsolutePath()
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 출력 읽기 (별도 스레드)
            Thread installReader = new Thread(() -> {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // 중요한 메시지만 로그에 출력 (너무 많은 로그 방지)
                        if (line.contains("Installing") || line.contains("Successfully") || 
                            line.contains("ERROR") || line.contains("WARNING") ||
                            line.contains("Collecting") && line.contains("torch")) {
                            log.info("pip 설치: {}", line);
                        }
                    }
                } catch (Exception e) {
                    // 스트림이 닫혔을 때 정상 종료
                }
            });
            installReader.setDaemon(true);
            installReader.start();
            
            // 설치 완료까지 대기
            int exitCode = process.waitFor();
            installReader.join(1000); // 출력 스레드가 완료될 때까지 최대 1초 대기
            
            if (exitCode == 0) {
                log.info("패키지 설치가 완료되었습니다.");
                return true;
            } else {
                log.error("패키지 설치 실패. 종료 코드: {}", exitCode);
                return false;
            }
        } catch (Exception e) {
            log.error("패키지 설치 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 프로젝트 루트 디렉토리 찾기
     */
    private String findProjectRoot() {
        // 현재 작업 디렉토리에서 시작
        File currentDir = new File(System.getProperty("user.dir"));
        
        // build.gradle 파일이 있는 디렉토리를 찾음
        File dir = currentDir;
        while (dir != null) {
            File buildFile = new File(dir, "build.gradle");
            if (buildFile.exists()) {
                return dir.getAbsolutePath();
            }
            dir = dir.getParentFile();
        }
        
        return currentDir.getAbsolutePath();
    }

    /**
     * 애플리케이션 종료 시 FastAPI 서버도 종료
     */
    @PreDestroy
    public void stopFastApiServer() {
        if (fastApiProcess != null && fastApiProcess.isAlive()) {
            log.info("FastAPI 서버를 종료합니다. PID: {}", fastApiProcess.pid());
            fastApiProcess.destroy();
            
            // 5초 후에도 종료되지 않으면 강제 종료
            try {
                Thread.sleep(5000);
                if (fastApiProcess.isAlive()) {
                    fastApiProcess.destroyForcibly();
                    log.warn("FastAPI 서버를 강제 종료했습니다.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fastApiProcess.destroyForcibly();
            }
        }
    }
}

