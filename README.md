# kimting

사람의 디지털 삶을 기억하는 오픈소스 Personal Memory Engine

---

## 소개

카카오톡 대화, 이메일, 일정 등 개인의 디지털 기록은 플랫폼마다 흩어져 있고, AI는 그 맥락을 모른다. kimting은 흩어진 개인 기록을 구조화된 Memory로 저장하고, AI가 사람의 삶과 관계를 이해할 수 있도록 기억을 제공하는 엔진이다.

> "창수가 누구야?" — 단톡방에서 처음 보는 이름이 나와도, kimting이 있으면 AI가 이전 대화를 바탕으로 대답한다.

## 특징

- **플러그인 임포터** — 카카오톡(수동), Telegram(자동) 등 개인 데이터를 Memory로 변환
- **Memory API** — store, search, timeline, strengthen, forget
- **복합 랭킹** — 벡터 유사도 + 중요도 + 최신성 + 신뢰도
- **로컬 우선** — 개인 데이터를 외부 서버로 전송하지 않음
- **MCP 지원** — Claude Desktop 등 MCP 클라이언트와 연결 가능

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
| 2주차 | KakaoTalk 임포터 (수동 .txt 파싱) | 진행 중 |
| 3주차 | REST API 완성 + 자연어 Memory 파싱 | 예정 |
| 4주차 | Memory 랭킹/검색 고도화 + SDK | 예정 |
| 5주차 | Telegram 임포터 (자동 수집) + CLI | 예정 |
| 6주차 | MCP + 확인 흐름 + 문서 + 대회 제출 | 예정 |

## 라이선스

MIT
