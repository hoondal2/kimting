# kimting — 나만의 AI와 대화하세요

내 기억을 직접 저장하고, AI가 그것을 기억하며 대화합니다.  
Ollama + Docker로 내 PC에서 무료로 실행됩니다. 외부 서버 불필요.

![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)
![Ollama](https://img.shields.io/badge/Ollama-로컬_LLM-black?style=flat-square)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)

---

## 사전 준비

kimting을 실행하기 전에 아래 두 가지를 설치해야 합니다.

| 필수 소프트웨어 | 역할 | 설치 방법 |
|---|---|---|
| **Ollama** | 로컬 AI 실행 엔진 | [ollama.com](https://ollama.com) 에서 다운로드 후 설치 |
| **Docker Desktop** | DB + 앱 컨테이너 실행 | [docker.com](https://www.docker.com/products/docker-desktop/) 에서 다운로드 후 설치 |

> **사양 안내** — RAM 8GB 이상, 저장 공간 5GB 이상 필요합니다. GPU 없이도 동작하지만, CPU만 사용 시 응답에 10~30초 소요될 수 있습니다.

---

## 빠른 시작

### 1단계 — Ollama AI 모델 다운로드

터미널을 열고 아래 명령어를 실행하세요. 처음 한 번만 하면 됩니다.

```bash
# 채팅 모델 (~1.6GB)
ollama pull gemma2:2b

# 기억 검색용 임베딩 모델 (~274MB)
ollama pull nomic-embed-text
```

> 다운로드 후 Ollama가 백그라운드에서 실행 중인지 확인하세요. 시스템 트레이에 Ollama 아이콘이 보이면 정상입니다.

### 2단계 — 저장소 다운로드 및 환경 설정

```bash
git clone https://github.com/your-username/kimting.git
cd kimting

# 환경 설정 파일 생성 (기본값 그대로 사용 가능)
cp .env.example .env
```

필요하다면 `.env`를 열어 DB 비밀번호를 변경할 수 있습니다. 기본값으로도 로컬 실행에는 문제 없습니다.

```env
# DB 비밀번호 (원하는 값으로 변경 가능)
DB_PASSWORD=kimting_local

# Ollama 주소 (Windows/Mac은 기본값 사용)
OLLAMA_BASE_URL=http://host.docker.internal:11434
```

### 3단계 — Docker로 실행

아래 명령어 하나로 DB와 앱이 동시에 실행됩니다. 처음에는 빌드 시간이 3~5분 걸립니다.

```bash
docker compose up --build
```

빌드가 완료되면 브라우저에서 **http://localhost:8080** 으로 접속하세요.

---

## 사용 방법

| 기능 | 설명 |
|---|---|
| **회원가입 / 로그인** | 계정을 만들면 기억이 개인별로 격리되어 저장됩니다. |
| **AI 채팅** | AI와 대화를 나눕니다. 저장된 기억을 바탕으로 답변합니다. |
| **기억 저장** | AI와 대화 중 중요한 내용은 자동으로 기억에 저장됩니다. 직접 기억을 추가하거나 수정할 수도 있습니다. |
| **기억 검색** | 저장된 기억을 키워드로 검색합니다. 의미 기반(벡터) 검색이 적용됩니다. |
| **카카오톡 대화 가져오기** | 카카오톡 대화 내보내기 파일을 업로드하면 AI가 기억을 자동으로 추출합니다. |

---

## 앱 종료 및 재시작

```bash
# 종료
docker compose down

# 재시작 (빌드 불필요)
docker compose up

# 데이터(기억, 계정)까지 초기화하고 싶을 때
docker compose down -v
```

> **주의** — `docker compose down -v`는 저장된 모든 기억과 계정을 삭제합니다. 신중하게 사용하세요.

---

## 기술 스택

| 분류 | 기술 |
|---|---|
| **Backend** | Spring Boot 3.5, Spring AI 1.1, Spring Security, JWT |
| **Frontend** | React 19, Vite |
| **AI / LLM** | Ollama (로컬), gemma2:2b, nomic-embed-text, RAG 파이프라인 |
| **Database** | PostgreSQL 16, pgvector, HNSW 인덱스 |
| **DevOps** | Docker Compose, 멀티스테이지 빌드, Gradle |
| **Architecture** | DDD + Hexagonal Architecture, MCP 서버 내장 |

---

## 개발 환경 직접 실행 (선택)

Docker 없이 직접 실행하고 싶다면 아래 순서로 진행하세요.

**필요한 것**
- Java 21
- Node.js 20+
- PostgreSQL 15+ (pgvector 확장 설치 필요)
- Ollama (위 모델 2개 다운로드된 상태)

`src/main/resources/application.yaml`에서 데이터소스를 로컬 DB로 맞춘 후:

```bash
# 백엔드 실행
./gradlew bootRun
```

```bash
# 프론트엔드 실행 (별도 터미널)
cd frontend
npm install
npm run dev
```

프론트엔드는 `http://localhost:5173`, 백엔드는 `http://localhost:8080` 에서 실행됩니다.

---

## 자주 묻는 문제

**Q. AI가 "연결할 수 없습니다" 오류를 냅니다.**  
Ollama가 실행 중인지 확인하세요. 시스템 트레이에 Ollama 아이콘이 없다면 Ollama를 다시 시작하세요.

**Q. 첫 응답이 너무 오래 걸립니다.**  
GPU가 없으면 처음 로딩에 30초 이상 걸릴 수 있습니다. 두 번째 메시지부터는 더 빠릅니다.

**Q. `docker compose up`이 DB 연결 오류를 냅니다.**  
DB가 준비되기 전에 앱이 먼저 시작될 수 있습니다. 잠시 후 자동으로 재시도됩니다. 계속 오류가 난다면 `docker compose restart app`을 실행하세요.

**Q. 기존에 Gemini 버전으로 저장된 기억이 검색이 안 됩니다.**  
Gemini와 Ollama는 다른 벡터 공간을 사용합니다. `docker compose down -v`로 초기화 후 다시 시작하면 됩니다.

---

MIT License
