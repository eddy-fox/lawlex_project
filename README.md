# Team Project

## 카테고리 추천 모델 설정 가이드

### 필수 요구사항

1. **Python 3.11 설치**
   - Python 3.13은 `tokenizers` 패키지와 호환성 문제가 있어 사용 불가
   - [Python 3.11 다운로드](https://www.python.org/downloads/release/python-3110/)

2. **모델 파일 준비**
   - `best_kobert_model.pt` 파일을 `src/main/java/com/soldesk/team_project/python/model/` 폴더에 배치
   - 이 파일은 GitHub에 포함되지 않으므로 별도로 공유받아야 합니다

### 설치 방법

1. **Python 패키지 설치**
   ```bash
   cd src/main/java/com/soldesk/team_project/python/model
   py -3.11 -m pip install -r requirements.txt
   ```
   
   또는 Windows에서:
   ```bash
   python3.11 -m pip install -r requirements.txt
   ```

2. **application.properties 설정**
   
   Windows (Python Launcher 사용):
   ```properties
   fastapi.server.python-command=py -3.11
   ```
   
   Windows (전체 경로 지정):
   ```properties
   fastapi.server.python-command=C:\Python311\python.exe
   ```
   
   Linux/Mac:
   ```properties
   fastapi.server.python-command=python3.11
   ```

3. **애플리케이션 실행**
   - Spring Boot 애플리케이션을 실행하면 FastAPI 서버가 자동으로 시작됩니다
   - FastAPI 서버는 `http://localhost:8000`에서 실행됩니다

### 문제 해결

- **패키지 설치 실패 시**: Python 3.11이 정확히 설치되었는지 확인
  ```bash
  py -3.11 --version
  # 또는
  python3.11 --version
  ```

- **FastAPI 서버가 시작되지 않을 때**: 
  - `application.properties`의 `fastapi.server.python-command` 경로 확인
  - 수동으로 서버 실행 테스트:
    ```bash
    cd src/main/java/com/soldesk/team_project/python/model
    py -3.11 -m uvicorn server:app --host 0.0.0.0 --port 8000
    ```

- **모델 파일이 없다는 오류**: `best_kobert_model.pt` 파일이 올바른 위치에 있는지 확인

