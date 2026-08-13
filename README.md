# kimting

사람의 디지털 삶을 기억하는 오픈소스 Personal Memory Engine

---

## 소개

기존 AI 챗봇은 대화 세션이 끝나면 모든 맥락을 잃는다. "지난주에 말한 회의 일정", "내가 싫어하는 음식", "친구 생일"을 매번 다시 설명해야 한다.

kimting은 대화, 일정, 감정, 관계 등 사용자의 디지털 기억을 구조화하여 저장하고, AI 응답 생성 시 자동으로 활용하는 **개인 기억 엔진**이다.

> "창수가 누구야?" — 단톡방에서 처음 보는 이름이 나와도, kimting이 있으면 AI가 이전 대화를 바탕으로 대답한다.

---

## 주요 기능

- **자연어 Memory 파싱** — "나 내일 10시에 팀 회의 있어"를 자동으로 `SCHEDULE` 타입 구조체로 변환 (Gemini LLM + 규칙 기반 폴백)
- **RAG 기반 개인화 채팅** — 메시지 수신 시 관련 기억을 벡터 검색 후 시스템 프롬프트에 주입, 새 기억은 자동 저장
- **복합 랭킹 알고리즘** — 벡터 유사도 × 0.5 + 중요도 × 0.2 + 최신성(지수 감쇠) × 0.2 + 신뢰도 × 0.1
- **KakaoTalk 임포터** — 카카오톡 대화 내보내기(`.txt`)를 Memory로 일괄 변환
- **MCP 서버** — Claude Desktop에서 kimting 기억을 직접 조회
- **JWT 인증 + 유저 격리** — SQL 레이어와 pgvector 메타데이터 필터 양쪽에서 이중 격리

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.16 |
| AI 프레임워크 | Spring AI 1.1.0 |
| LLM | Google Gemini (`gemini-2.0-flash-lite`) — OpenAI 호환 엔드포인트 |
| 임베딩 | Google `gemini-embedding-001` (768차원) |
| 벡터 DB | pgvector (PostgreSQL 16, Docker) — HNSW 인덱스, COSINE_DISTANCE |
| 인증 | Spring Security + JJWT 0.12.6 (HS256, Stateless) |
| 프론트엔드 | Vite 5 + React 18 |
| 프로토콜 | REST API, MCP (Model Context Protocol) |

---

## 시작하기

### 사전 요구사항

- Java 21
- Docker
- [Google AI Studio](https://aistudio.google.com) API 키

### 백엔드 실행

```bash
# 1. 저장소 클론
git clone https://github.com/hoondal2/kimting.git
cd kimting

# 2. 설정 파일 복사
cp src/main/resources/application.yaml.example src/main/resources/application.yaml
```

`application.yaml`을 열어 다음 값을 채운다:

```yaml
spring:
  datasource:
    password: your_db_password
  ai:
    openai:
      api-key: your-gemini-api-key   # aistudio.google.com → Get API key

kimting:
  jwt:
    secret: your-random-64-character-secret-string
```

```bash
# 3. pgvector 컨테이너 실행
docker-compose up -d

# 4. 백엔드 실행
./gradlew bootRun
```

백엔드는 `http://localhost:8080`에서 실행된다.

### 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

브라우저에서 `http://localhost:5173`으로 접속한다.

---

## API 요약

```
# 인증
POST   /api/auth/register          회원가입 (email, password, name)
POST   /api/auth/login             로그인 → JWT 반환

# 채팅 (Bearer 토큰 필요)
POST   /api/chat                   메시지 전송 → AI 응답 + 사용된/저장된 기억

# 기억 CRUD (Bearer 토큰 필요)
POST   /api/memories               직접 저장
GET    /api/memories               목록 (type, tag, limit 필터)
GET    /api/memories/{id}          단건 조회
PUT    /api/memories/{id}          수정
DELETE /api/memories/{id}          삭제

# 기억 검색
GET    /api/memories/search?query=...&topK=10    벡터 유사도 검색
GET    /api/memories/timeline?from=...&to=...    기간 조회
GET    /api/memories/people/{name}               인물 기준 조회
GET    /api/memories/recent                      최근 저장 기억

# 기억 강화 / 약화
POST   /api/memories/{id}/strengthen
POST   /api/memories/{id}/forget

# 자연어 파싱
POST   /api/memories/parse         자연어 → Memory 구조체 (미리보기)
POST   /api/memories/parse/confirm 파싱 결과 저장

# 임포터
POST   /api/memories/import/kakao  카카오톡 대화 파일 업로드
```

---

## MCP 연동 (Claude Desktop)

`claude_desktop_config.json`에 다음을 추가한다:

```json
{
  "mcpServers": {
    "kimting": {
      "command": "java",
      "args": ["-jar", "/path/to/kimting.jar", "--spring.ai.mcp.server.stdio=true"]
    }
  }
}
```

사용 가능한 MCP 툴:
- `searchMemory(query)` — 관련 기억 검색
- `recentMemories()` — 최근 기억 목록
- `memoriesByPerson(personName)` — 인물별 기억 조회

---

## 프로젝트 구조

```
kimting/
├── src/main/java/com/kimting/kimting/
│   ├── api/                  # REST 컨트롤러 + DTO
│   │   ├── AuthController.java
│   │   ├── ChatController.java
│   │   └── MemoryController.java
│   ├── core/
│   │   ├── domain/           # Memory, User 엔티티
│   │   ├── repository/       # JPA Repository
│   │   ├── service/          # 비즈니스 로직
│   │   └── ranking/          # 복합 랭킹 알고리즘
│   ├── parser/               # LLM / 규칙 기반 파서
│   ├── importer/             # KakaoTalk 임포터
│   ├── security/             # JWT 필터, SecurityConfig
│   ├── mcp/                  # MCP 툴 (Claude Desktop 연동)
│   └── config/               # Gemini 임베딩, CORS
├── src/main/resources/
│   ├── application.yaml.example   # 설정 템플릿 (공개)
│   └── application.yaml           # 실제 설정 (gitignored)
├── frontend/                 # Vite + React 프론트엔드
└── docker-compose.yml        # pgvector 컨테이너
```

---

## 개발 현황

| 주차 | 내용 | 상태 |
|------|------|------|
| 1주차 | Memory 모델 설계, pgvector, 임베딩 | ✅ 완료 |
| 2주차 | KakaoTalk 임포터, 벡터 검색, 복합 랭킹 알고리즘 | ✅ 완료 |
| 3주차 | REST API + 자연어 Memory 파싱 (LLM / 규칙 기반) | ✅ 완료 |
| 4주차 | ChatService + Gemini RAG 채팅 + MCP 서버 | ✅ 완료 |
| 5주차 | Vite + React 프론트엔드 (채팅, 기억 패널, 다크모드) | ✅ 완료 |
| 6주차 | JWT 인증 + 유저별 기억 격리 + 프론트 디자인 리뉴얼 | ✅ 완료 |
| 7주차 | 버그 수정 + 문서 정리 + 대회 제출 | 진행 중 |

---

## 라이선스

MIT
