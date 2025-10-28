# Dito Backend - Git 보안 가이드 📋

## ⚠️ 중요: Git에 커밋하기 전 반드시 읽으세요!

Jenkins 연결을 성공적으로 완료하셨습니다! 🎉  
이제 코드를 Git에 푸시하기 전에 보안 검증이 필요합니다.

---

## 🚨 현재 발견된 주요 보안 이슈

### 1. **절대 Git에 올리면 안 되는 파일들**

#### ❌ `.env` 파일
```bash
# 현재 상태: backend/.env 파일에 실제 비밀번호 포함
DB_PASSWORD=dito2025  # 실제 비밀번호!
REDIS_PASSWORD=dito2025  # 실제 비밀번호!

# 해결 방법:
git rm --cached backend/.env
```

#### ❌ `Dito/data/` 폴더 (PostgreSQL 데이터 파일)
```bash
# 이 폴더는 실제 DB 데이터를 포함하고 있습니다
# 절대 Git에 올리면 안 됩니다!

# 해결 방법:
git rm -r --cached backend/Dito/data
```

#### ❌ SSH 키 파일들
```bash
# jenkins_git_key, jenkins_git_key.pub 등
# 해결 방법:
git rm --cached jenkins_git_key*
```

---

## ✅ 즉시 실행해야 할 명령어

### 1. Git에서 민감 파일 제거
```bash
cd backend

# .env 파일 제거
git rm --cached .env

# data 폴더 제거
git rm -r --cached Dito/data

# SSH 키 제거 (있다면)
git rm --cached jenkins_git_key jenkins_git_key.pub 2>/dev/null || true

# 변경사항 확인
git status
```

### 2. 보안 검증 스크립트 실행
```bash
# Linux/Mac
chmod +x check-git-security.sh
./check-git-security.sh

# Windows
check-git-security.bat
```

### 3. .gitignore 적용 및 커밋
```bash
git add .gitignore
git add Dito/.gitignore
git add SECURITY_CHECKLIST.md
git commit -m "security: Update .gitignore and remove sensitive files"
```

---

## 📝 수정된 파일 목록

### 새로 생성된 파일
1. ✅ `backend/.gitignore` - 민감 파일 제외 규칙
2. ✅ `backend/Dito/.gitignore` - 업데이트 (data/ 폴더 제외)
3. ✅ `backend/.env.example` - 안전한 환경변수 템플릿
4. ✅ `backend/SECURITY_CHECKLIST.md` - 보안 체크리스트
5. ✅ `backend/check-git-security.sh` - 보안 검증 스크립트 (Linux/Mac)
6. ✅ `backend/check-git-security.bat` - 보안 검증 스크립트 (Windows)

### 수정된 파일
1. ✅ `backend/Dito/src/main/resources/application-local.yml` - 하드코딩 비밀번호 제거 (dito2025 → changeme)

---

## 🔒 Git에 올려도 되는 것 vs 안 되는 것

### ✅ 커밋해도 되는 것
- `Dockerfile` ✅
- `docker-compose.yml` ✅ (환경변수 참조만 있음)
- `Jenkinsfile` ✅
- `JENKINS_SETUP.md` ✅
- `.env.example` ✅ (실제 비밀번호 없음)
- `src/` 폴더 (소스 코드) ✅
- `build.gradle`, `settings.gradle` ✅
- `application-*.yml` ✅ (환경변수 참조, 하드코딩 없음)

### ❌ 절대 커밋하면 안 되는 것
- `.env` ❌ (실제 비밀번호 포함)
- `Dito/data/` ❌ (PostgreSQL 데이터)
- `jenkins_git_key*` ❌ (SSH 키)
- `*.pem`, `*.key` ❌ (인증서/키)
- `build/`, `.gradle/` ❌ (빌드 산출물)

---

## 🔍 커밋 전 최종 확인

```bash
# 1. Git 상태 확인
git status

# 2. 민감정보 검색
git diff --cached | grep -i "password\|secret\|key\|token"

# 3. .env 파일이 추적되고 있는지 확인
git ls-files | grep .env

# 4. data 폴더가 추적되고 있는지 확인
git ls-files | grep data/

# 5. 보안 검증 스크립트 실행
./check-git-security.sh  # 또는 check-git-security.bat
```

---

## 🚀 안전한 커밋 워크플로우

```bash
# 1. 보안 검증
./check-git-security.sh

# 2. 변경사항 확인
git status
git diff

# 3. 안전한 파일만 추가
git add src/
git add *.gradle
git add Dockerfile docker-compose.yml Jenkinsfile
git add .gitignore SECURITY_CHECKLIST.md

# 4. .env, data/ 같은 민감 파일은 절대 추가하지 않음!
# git add .env  ❌ 절대 실행 금지!
# git add Dito/data/  ❌ 절대 실행 금지!

# 5. 커밋
git commit -m "feat: your commit message"

# 6. 푸시 전 최종 확인
git log -1 -p | grep -i "password\|secret"

# 7. 푸시
git push origin develop
```

---

## ⚡ 빠른 수정 가이드

### 실수로 .env를 커밋했다면?
```bash
# 1. Git 캐시에서 제거
git rm --cached .env

# 2. .gitignore에 추가 (이미 되어 있음)
echo ".env" >> .gitignore

# 3. 커밋
git add .gitignore
git commit -m "security: Remove .env from git tracking"

# 4. 푸시
git push origin develop

# 5. (중요!) 노출된 비밀번호는 즉시 변경하세요!
```

### 실수로 data/ 폴더를 커밋했다면?
```bash
git rm -r --cached Dito/data
git add Dito/.gitignore
git commit -m "security: Remove data folder from git tracking"
git push origin develop
```

---

## 📞 도움이 필요하다면

### 문제 1: "이미 .env를 푸시했어요!"
1. **즉시** 해당 비밀번호를 변경하세요
2. Git 히스토리에서 제거:
   ```bash
   # BFG Repo-Cleaner 사용 (권장)
   bfg --delete-files .env
   git reflog expire --expire=now --all
   git gc --prune=now --aggressive
   git push --force
   ```

### 문제 2: "보안 검증 스크립트가 실패해요"
1. 스크립트 실행 권한 확인:
   ```bash
   chmod +x check-git-security.sh
   ```
2. 오류 메시지를 확인하고 해당 파일 제거

### 문제 3: ".gitignore가 작동하지 않아요"
```bash
# Git 캐시 완전 초기화 후 재추가
git rm -r --cached .
git add .
git commit -m "Fix .gitignore"
```

---

## 📚 추가 리소스

- [SECURITY_CHECKLIST.md](./SECURITY_CHECKLIST.md) - 상세 보안 체크리스트
- [JENKINS_SETUP.md](./JENKINS_SETUP.md) - Jenkins CI/CD 설정 가이드
- [.env.example](./.env.example) - 환경변수 템플릿

---

## ✨ 마무리 체크리스트

커밋하기 전에 다음 항목을 모두 확인하세요:

- [ ] `.env` 파일이 Git에서 제외되었는지 확인
- [ ] `Dito/data/` 폴더가 Git에서 제외되었는지 확인
- [ ] SSH 키 파일들이 Git에서 제외되었는지 확인
- [ ] `application-*.yml` 파일에 하드코딩된 비밀번호가 없는지 확인
- [ ] `check-git-security.sh` 스크립트가 통과하는지 확인
- [ ] `git status`로 커밋할 파일 목록 최종 확인
- [ ] `git diff --cached`로 변경 내용 확인

모든 항목을 확인했다면 안전하게 푸시할 수 있습니다! 🚀

---

**⚠️ 기억하세요**: 한 번 Git에 푸시된 민감정보는 히스토리에 영구적으로 남습니다.  
항상 커밋 전에 확인하는 습관을 들이세요!

