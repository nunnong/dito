# Jenkins CI/CD 설정 가이드

## 📋 개요

GitLab의 `backend` 브랜치에 merge될 때마다 자동으로 빌드 및 배포되는 CI/CD 파이프라인 설정 가이드입니다.

### 주요 특징
- ✅ 멀티스테이지 빌드로 경량화된 Docker 이미지
- ✅ Jenkins에서 환경 변수 중앙 관리
- ✅ GitLab Webhook 자동 트리거
- ✅ Docker Compose를 통한 통합 배포
- ✅ PostgreSQL, Redis 포함 완전한 스택

---

## 🚀 사전 요구사항

### 1. 서버 환경
```bash
# Docker 설치 확인
docker --version
# Docker version 24.0.0 이상 권장

# Docker Compose 설치 확인
docker-compose --version
# Docker Compose version 2.20.0 이상 권장

# Jenkins 설치 확인
systemctl status jenkins
```

### 2. Jenkins 플러그인 설치
Jenkins 관리 > 플러그인 관리에서 다음 플러그인 설치:

- **GitLab Plugin** - GitLab 연동
- **Docker Pipeline Plugin** - Docker 파이프라인
- **Credentials Binding Plugin** - 환경 변수 관리
- **Pipeline Plugin** - 파이프라인 기능
- **Git Plugin** - Git 연동

---

## 🔧 1단계: Jenkins 서버 Docker 설정

### Docker 그룹에 Jenkins 사용자 추가
```bash
# Jenkins 사용자를 docker 그룹에 추가
sudo usermod -aG docker jenkins

# Jenkins 재시작
sudo systemctl restart jenkins

# 권한 확인
sudo -u jenkins docker ps
```

---

## 🔐 2단계: Jenkins Credentials 설정

### 2.1 GitLab Access Token 등록

1. **GitLab에서 Access Token(PAT) 생성 — 의미와 권장 스코프**
   - 설명: PAT(Personal Access Token)는 GitLab 계정 대신 사용하는 긴 임시 비밀번호로, CI/CD에서 안전하게 인증하기 위해 사용합니다. 일반 비밀번호 대신 토큰을 사용하면 권한 범위(scope)를 제한하고 필요시 폐기할 수 있어 안전합니다.
   - 권장 스코프:
     - `read_repository` (필수) — 레포지토리 클론/조회
     - `write_repository` (필요 시) — 푸시 권한
     - `api` (Jenkins GitLab 플러그인/관리 API 호출 필요 시)

   GitLab UI에서:
   - User 메뉴 → Edit profile (또는 Settings) → Access Tokens
   - Name: `jenkins-ci`
   - Expires at: (옵션)
   - Scopes: 위 권장 스코프 선택
   - Create token → 생성된 토큰값을 복사(한 번만 표시되므로 반드시 복사)

2. **Jenkins에 등록 (권장: Username with password)**
   - 이유: Jenkins SCM UI(Repository URL 입력란)가 Username+Password 형태를 기대하므로, PAT를 Password 필드에 넣는 방식이 가장 호환성이 좋습니다.
   - Jenkins UI에서:
     - Jenkins → Credentials → System → Global credentials (unrestricted) → Add Credentials
     - Kind: `Username with password`
     - Username: (GitLab 사용자 아이디, 예: your-username)
     - Password: (복사한 PAT 값을 붙여넣기)
     - ID: `gitlab-https-cred` (또는 식별용 임의 ID)
     - Description: `GitLab PAT for repo access`
   - 저장

3. **Job 설정에서 적용**
   - Job → Configure → SCM(Repository URL) 입력: `https://lab.ssafy.com/s13-final/S13P31A708.git`
   - Credentials: 방금 추가한 `gitlab-https-cred` 선택
   - Branch Specifier: `*/backend` (또는 필요한 브랜치)
   - Save → Build 시도

4. **컨테이너(또는 Jenkins 에이전트)에서 직접 인증 테스트 (권장, 토큰 노출 주의)**
   - Git 인증 문제 해결을 위해 컨테이너 내부에서 바로 확인할 수 있습니다. 토큰은 절대 공개 채팅에 붙여넣지 마세요.

   예: Jenkins 컨테이너 이름이 `dito-jenkins`인 경우

   ```bash
   # HTTPS 방식 테스트 (실제 토큰으로 <TOKEN>을 대체)
   docker exec -it dito-jenkins bash -lc \
     "git ls-remote https://<GITLAB_USERNAME>:<PERSONAL_ACCESS_TOKEN>@lab.ssafy.com/s13-final/S13P31A708.git HEAD"
   ```

   - 성공: HEAD 해시 값 출력
   - 실패: 인증 오류/권한 부족 메시지 확인 → 토큰 스코프 또는 Username 확인

5. **대안: SSH 방식 (장기 운영 권장)**
   - SSH 키 생성:
     ```bash
     ssh-keygen -t ed25519 -C "jenkins-ci" -f jenkins_git_key -N ""
     # jenkins_git_key (private), jenkins_git_key.pub (public)
     ```
   - GitLab에 public 키 등록: Project Settings → Deploy Keys 또는 User Settings → SSH Keys
   - Jenkins에 private key 등록:
     - Jenkins → Credentials → System → Global → Add Credentials
     - Kind: `SSH Username with private key`
     - Username: `git` (GitLab의 SSH 사용자)
     - Private Key: Enter directly → (jenkins_git_key 내용 붙여넣기)
     - ID: `gitlab-ssh-cred`
   - Repository URL을 SSH 형식으로 변경: `git@lab.ssafy.com:s13-final/S13P31A708.git`

6. **자주 발생하는 인증 실패 원인 정리**
   - Credentials가 Job에 선택되지 않음 → Job 구성에서 반드시 선택
   - 토큰이 만료됐거나 잘못 복사됨(공백 포함) → 토큰 재발급
   - 토큰 스코프 부족(최소 `read_repository`) → 스코프 재설정 후 재발급
   - URL과 Credential 타입 불일치(HTTPS URL에 SSH 키 선택 등) → URL/크레덴셜 일치 확인
   - GitLab이 IP 제한/Proxy 설정을 요구 → 네트워크 관리자에게 문의

7. **Jenkinsfile의 triggers / Webhook Secret 관계**
   - Jenkinsfile의 triggers 블록에서 `secretToken: env.GITLAB_WEBHOOK_SECRET`을 사용하면, Jenkins 전역 환경변수나 Credentials로 `GITLAB_WEBHOOK_SECRET` 값이 제공되어야 합니다.
   - 간단한 방법: Manage Jenkins → Configure System → Global properties → Environment variables에 `GITLAB_WEBHOOK_SECRET`을 직접 추가 (또는 Credentials로 관리 후 파이프라인에서 읽어오기)

### 2.2 환경 변수 파일 (.env) 등록

1. **환경 변수 파일 생성**
   ```bash
   # backend/.env.example을 참고하여 실제 값으로 작성
   cp backend/.env.example backend/.env.production
   vim backend/.env.production
   ```

2. **Jenkins에 Secret File로 등록**
   - Jenkins > Credentials > System > Global credentials
   - Kind: `Secret file`
   - File: `.env.production` 업로드
   - ID: `dito-backend-env` ⚠️ **Jenkinsfile의 ENV_FILE과 일치해야 함**
   - Description: `Dito Backend Environment Variables`

### 2.3 GitLab Webhook Secret 등록

1. **Jenkins에 Secret Text 등록**
   - Jenkins > Credentials > System > Global credentials
   - Kind: `Secret text`
   - Secret: [랜덤 생성된 토큰 - 예: `openssl rand -hex 32`]
   - ID: `gitlab-webhook-secret`
   - Description: `GitLab Webhook Secret`

---

## 📦 3단계: Jenkins Pipeline Job 생성

### 3.1 새 Item 생성
1. Jenkins 대시보드 > New Item
2. Item name: `dito-backend-cicd`
3. Type: `Pipeline`
4. OK 클릭

### 3.2 General 설정
- ✅ **GitLab Connection**
  - GitLab connection: [설정한 GitLab 연결 선택]

### 3.3 Build Triggers 설정
- ✅ **Build when a change is pushed to GitLab**
  - Push Events: 체크
  - Accepted Merge Request Events: 체크
  - Allowed branches: `Filter branches by name` → `backend`
  - Secret token: `Generate` 클릭 후 토큰 복사 (GitLab Webhook 설정에 사용)

### 3.4 Pipeline 설정
- **Definition**: `Pipeline script from SCM`
- **SCM**: `Git`
- **Repository URL**: `https://lab.ssafy.com/your-group/your-repo.git`
- **Credentials**: [GitLab Access Token 선택]
- **Branch Specifier**: `*/backend`
- **Script Path**: `backend/Jenkinsfile`

### 3.5 저장
- `Save` 클릭

---

## 🔗 4단계: GitLab Webhook 설정

### 4.1 GitLab 프로젝트 설정
1. GitLab 프로젝트 > Settings > Webhooks
2. **URL**: `http://[Jenkins서버IP]:8080/project/dito-backend-cicd`
   - 예: `http://10.0.0.100:8080/project/dito-backend-cicd`
3. **Secret Token**: [Jenkins에서 생성한 Secret Token]
4. **Trigger**:
   - ✅ Push events: `backend`
   - ✅ Merge request events
5. **SSL verification**: 필요시 비활성화 (내부 서버인 경우)
6. `Add webhook` 클릭

### 4.2 Webhook 테스트
- Test > Push events 클릭
- HTTP 200 응답 확인

---

## 🌐 5단계: Jenkins 환경 변수 설정

### 5.1 Global Properties 설정
Jenkins > Manage Jenkins > Configure System

```groovy
Environment variables:
- Name: GITLAB_WEBHOOK_SECRET
  Value: [Credentials에서 참조: ${GITLAB_WEBHOOK_SECRET}]
```

### 5.2 Jenkinsfile에서 사용 가능한 환경 변수
```groovy
environment {
    ENV_FILE = credentials('dito-backend-env')  // .env 파일
}
```

---

## 📊 6단계: 배포 확인

### 6.1 수동 빌드 테스트
1. Jenkins Job > Build Now 클릭
2. Console Output 확인
3. 빌드 성공 확인

### 6.2 배포 상태 확인
```bash
# 서버에서 실행
cd /path/to/project/backend

# 실행 중인 컨테이너 확인
docker-compose ps

# 로그 확인
docker-compose logs -f app

# 헬스체크
curl http://localhost:8080/actuator/health
```

### 6.3 자동 배포 테스트
```bash
# 로컬에서 테스트 커밋
git checkout backend
echo "test" >> test.txt
git add test.txt
git commit -m "test: Jenkins CI/CD 테스트"
git push origin backend

# Jenkins에서 자동으로 빌드가 트리거되는지 확인
```

---

## 🔍 7단계: 트러블슈팅

### 문제 1: Docker 권한 오류
```bash
# 증상
Got permission denied while trying to connect to the Docker daemon socket

# 해결
sudo usermod -aG docker jenkins
sudo systemctl restart jenkins
```

### 문제 2: GitLab Webhook 연결 실패
```bash
# 확인사항
1. Jenkins 방화벽 포트 8080 오픈 확인
2. GitLab에서 Jenkins 서버 IP 접근 가능 확인
3. Webhook Secret Token 일치 확인
```

### 문제 3: 환경 변수 로드 실패
```bash
# 확인사항
1. Jenkins Credentials ID가 'dito-backend-env'인지 확인
2. .env 파일 형식이 올바른지 확인
3. Jenkinsfile의 credentials() 함수 확인
```

### 문제 4: 헬스체크 실패
```bash
# Spring Boot Actuator 의존성 확인
# build.gradle에 추가
implementation 'org.springframework.boot:spring-boot-starter-actuator'

# application.yml 설정
management:
  endpoints:
    web:
      exposure:
        include: health
```

### 문제 5: 포트 충돌
```bash
# 기존 컨테이너 확인 및 제거
docker ps -a
docker-compose down

# 포트 사용 확인
sudo netstat -tulpn | grep 8080
```

---

## 📈 8단계: 고급 설정 (선택사항)

### 8.1 빌드 알림 설정
- Slack/Email 플러그인 설치
- Jenkinsfile post 섹션에 알림 추가

### 8.2 멀티 브랜치 파이프라인
- dev, staging, prod 환경별 분리 배포

### 8.3 Blue-Green 배포
```yaml
# docker-compose.blue-green.yml 예시
services:
  app-blue:
    # ...
  app-green:
    # ...
  nginx:
    # 로드 밸런서
```

### 8.4 백업 전략
```bash
# 정기 백업 스크립트
#!/bin/bash
docker exec dito-postgres pg_dump -U postgres dito > backup_$(date +%Y%m%d).sql
```

---

## 📝 체크리스트

배포 전 최종 확인사항:

- [ ] Docker, Docker Compose 설치 확인
- [ ] Jenkins 플러그인 설치 완료
- [ ] Jenkins docker 그룹 권한 설정
- [ ] GitLab Access Token 등록
- [ ] .env 파일 Secret File로 등록 (ID: `dito-backend-env`)
- [ ] GitLab Webhook Secret 등록
- [ ] Jenkins Pipeline Job 생성
- [ ] GitLab Webhook 설정 및 테스트
- [ ] 수동 빌드 테스트 성공
- [ ] 자동 배포 테스트 성공
- [ ] 헬스체크 엔드포인트 확인

---

## 🎯 파이프라인 플로우

```
┌─────────────────────────────────────────────────────────────┐
│ 1. GitLab에 backend 브랜치로 Push/Merge                      │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 2. GitLab Webhook이 Jenkins 트리거                           │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 3. Jenkins: 소스 코드 Checkout                               │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 4. Jenkins: .env 파일 로드 (Credentials)                     │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 5. Docker: 멀티스테이지 빌드로 경량 이미지 생성              │
│    - Stage 1: Gradle 빌드                                   │
│    - Stage 2: JRE 기반 실행 이미지                          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 6. Docker: 기존 컨테이너 중지 및 제거                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 7. Docker Compose: 새 컨테이너 배포                          │
│    - PostgreSQL                                             │
│    - Redis                                                  │
│    - Spring Boot App                                        │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│ 8. 헬스체크 및 배포 완료                                      │
└─────────────────────────────────────────────────────────────┘
```

---

## 📞 지원

문제 발생 시:
1. Jenkins Console Output 확인
2. Docker 로그 확인: `docker-compose logs -f`
3. GitLab CI/CD 설정 확인

---

**작성일**: 2025-10-27
**버전**: 1.0
**담당자**: Infrastructure Team
