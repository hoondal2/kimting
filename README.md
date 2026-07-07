# kimting

AI 앱에 장기 기억을 제공하는 오픈소스 Personal Memory Engine

---

## 소개

LLM은 컨텍스트 윈도우가 닫히면 기억이 사라진다. kimting은 대화, 일정, 메일 등 흩어진 개인 기록을 구조화된 Memory로 저장하고, AI 앱이 필요할 때 꺼내 쓸 수 있는 Memory API와 MCP 서버를 제공한다.

## 특징

- **Memory API** — store, search, timeline, strengthen, forget
- **복합 랭킹** — 벡터 유사도 + 중요도 + 최신성 + 신뢰도
- **플러그인 임포터** — 카카오톡, 메일 등 다양한 플랫폼 지원 구조
- **MCP 서버** — Claude Desktop 등 MCP 클라이언트와 바로 연결
- **로컬 우선** — 개인 데이터를 외부 서버로 전송하지 않음

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.16 |
| AI 프레임워크 | Spring AI 1.1.0 |
| 임베딩 모델 | ko-sroberta-multitask (ONNX, 로컬) |
| 벡터 DB | pgvector (PostgreSQL 16) |

## 시작하기

```bash
# 1. 저장소 클론
git clone https://github.com/hoondal2/kimting.git
cd kimting

# 2. 설정 파일 복사 후 DB 비밀번호 설정
cp src/main/resources/application.yaml.example \
   src/main/resources/application.yaml

# 3. pgvector 실행
docker-compose up -d

# 4. 애플리케이션 실행
./gradlew bootRun
```

> **사전 요구사항**: Java 21, Docker

## 프로젝트 구조

```
src/main/java/com/kimting/kimting/
├── core/           # Memory 엔티티, 랭킹, 서비스
├── importer/       # 플러그인 임포터 인터페이스
│   └── kakao/      # 카카오톡 임포터
├── api/            # REST API
└── mcp/            # MCP 서버 Tool
```

## 개발 현황

| 주차 | 내용 | 상태 |
|------|------|------|
| 1주차 | Memory 모델 설계 + 프로젝트 구조 | ✅ 완료 |
| 2주차 | KakaoTalk 임포터 구현 | 진행 중 |
| 3주차 | Memory 랭킹 + 검색 고도화 | 예정 |
| 4주차 | REST API + Timeline 쿼리 | 예정 |
| 5주차 | MCP 서버 + Claude Desktop 연결 | 예정 |
| 6주차 | 문서 + 예제 + 배포 | 예정 |

## 라이선스

MIT
