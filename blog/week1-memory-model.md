# kimting 개발기 1주차 — Memory 모델 설계와 프로젝트 구조 리팩토링

> **kimting**은 AI 앱에 장기 기억을 제공하는 오픈소스 Personal Memory Engine이다.  
> 한국 오픈소스 개발자 대회 출품을 목표로 6주 스프린트로 개발 중이다.

---

## 왜 만드는가

LLM은 강력하지만 한 가지 구조적 한계를 갖는다. **컨텍스트 윈도우가 닫히면 기억이 사라진다.**

매 대화마다 "나는 이런 사람이고, 저번에 이런 일이 있었고…"를 반복해서 설명해야 한다. 카카오톡 대화, 이메일, 일정, 메모 등 수년치 개인 데이터는 서비스마다 흩어져 있고, 이걸 한꺼번에 컨텍스트로 넣는 건 불가능하다.

단순히 `memory.md`에 요약을 적어두는 방식도 있지만, 수년치 대화 기록을 텍스트 파일 하나에 욱여넣을 수는 없다. **의미 단위로 쪼개서 저장하고, 필요할 때 관련 기억만 꺼내오는 구조**가 필요하다.

kimting이 해결하려는 문제는 이것이다.

---

## 프로젝트 구조

1주차 목표는 **핵심 도메인 설계와 프로젝트 골격 구성**이다. 코드를 쓰기 전에 "Memory가 무엇인가"를 먼저 정의해야 했다.

최종적으로 잡은 패키지 구조는 다음과 같다.

```
com.kimting.kimting/
├── core/
│   ├── domain/       ← Memory 엔티티, MemoryType
│   ├── repository/   ← JPA 쿼리
│   ├── ranking/      ← 복합 랭킹 알고리즘
│   └── service/      ← 핵심 비즈니스 로직
├── importer/
│   ├── MemoryImporter.java    ← 플러그인 인터페이스
│   └── kakao/
│       └── KakaoImporter.java ← 카카오톡 파서 (2주차 구현)
├── api/
│   └── MemoryController.java  ← REST API
└── mcp/
    └── MemoryMcpTool.java     ← MCP 서버 Tool
```

---

## Memory 모델 설계

Memory는 단순한 텍스트 덩어리가 아니다. "사람의 기억"을 흉내내려면 다음 정보가 필요하다고 판단했다.

```java
@Entity
@Table(name = "memory")
public class Memory {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private MemoryType type;       // 기억의 종류

    private String title;          // 한 줄 요약 제목
    private String summary;        // 요약 (임베딩에 사용)
    private String content;        // 원문

    private List<String> people;   // 관련 인물
    private List<String> tags;     // 태그

    private Integer importance;    // 1~10 (중요도)
    private Double confidence;     // 0.0~1.0 (신뢰도)

    private LocalDateTime occurredAt; // 실제 발생 시점
    private String source;            // "kakao", "email", "manual"
}
```

### MemoryType

기억을 9가지 타입으로 분류했다.

| 타입 | 설명 |
|------|------|
| `CONVERSATION` | 대화 내용 |
| `SCHEDULE` | 약속, 일정 |
| `TODO` | 할 일 |
| `PREFERENCE` | 선호도, 취향 |
| `RELATIONSHIP` | 인물 관계 |
| `EVENT` | 발생한 사건 |
| `FACT` | 사실 정보 |
| `LONG_TERM` | 장기 기억 |
| `SHORT_TERM` | 단기 기억 |

### 이중 저장 구조

Memory는 두 곳에 저장된다.

```
JPA (memory 테이블)          Spring AI VectorStore
──────────────────          ───────────────────────
id, type, title,            id = Memory의 UUID
summary, content,           content = summary 또는 content
people, tags,         ←→   metadata = { type, title,
importance,                             importance, source }
confidence, source
```

**JPA**는 타입별·인물별·기간별 구조화 쿼리를 담당하고, **VectorStore**는 의미 기반 유사도 검색을 담당한다. 검색 시에는 VectorStore에서 UUID 목록을 받아 JPA로 전체 데이터를 로드한 뒤 랭킹을 적용한다.

---

## 복합 랭킹 알고리즘

벡터 유사도만으로 검색하면 "최근 저장된 중요한 기억"보다 "오래되었지만 텍스트가 더 비슷한 기억"이 우선순위를 가져갈 수 있다. 사람의 기억은 중요도와 시간에 영향을 받으므로 복합 점수를 설계했다.

```java
public static double score(Memory memory, double similarity) {
    double importanceScore = memory.getImportance() / 10.0;
    double recencyScore    = recency(memory.getOccurredAt());
    double confidenceScore = memory.getConfidence();

    return (similarity      * 0.5)
         + (importanceScore * 0.2)
         + (recencyScore    * 0.2)
         + (confidenceScore * 0.1);
}

private static double recency(LocalDateTime occurredAt) {
    if (occurredAt == null) return 0.5;
    long daysSince = ChronoUnit.DAYS.between(occurredAt, LocalDateTime.now());
    return Math.exp(-daysSince / 365.0); // 약 1년 기준 지수 감쇠
}
```

| 요소 | 가중치 | 설명 |
|------|--------|------|
| 벡터 유사도 | 0.5 | 쿼리와 의미적으로 얼마나 가까운가 |
| 중요도 | 0.2 | 사용자가 설정한 1~10 점수 |
| 최신성 | 0.2 | 최근 기억일수록 높음 (지수 감쇠, 약 1년 반감) |
| 신뢰도 | 0.1 | 기억의 정확성 (자동 추출은 낮게, 직접 입력은 높게) |

---

## Memory 라이프사이클

Memory는 삭제하지 않는다. 삭제 대신 중요도를 낮춘다.

```
Store → Strengthen(importance++) → Forget(importance--) → Recall
```

- **Store**: JPA + VectorStore에 동시 저장
- **Strengthen**: 중요하다고 판단되면 importance +1 (최대 10)
- **Forget**: 잊고 싶은 기억은 importance -1 (최소 1, 삭제 안 함)
- **Recall**: 복합 랭킹으로 정렬된 Memory 반환

---

## 플러그인 임포터 구조

다양한 플랫폼의 데이터를 Memory로 변환하는 임포터를 플러그인으로 설계했다.

```java
public interface MemoryImporter {
    String getSource();
    List<Memory> parse(InputStream input) throws IOException;
}
```

1주차에는 인터페이스만 정의하고, 카카오톡 임포터는 stub으로 남겼다. 2주차에서 `.txt` 내보내기 파일 파싱을 구현할 예정이다.

---

## REST API

```
POST   /api/memories              ← Memory 저장
GET    /api/memories/search       ← 시맨틱 검색 (query, topK)
GET    /api/memories/timeline     ← 기간별 조회 (from, to)
GET    /api/memories/people/{name} ← 인물별 조회
GET    /api/memories/recent       ← 최근 20개
POST   /api/memories/{id}/strengthen ← 중요도 +1
POST   /api/memories/{id}/forget     ← 중요도 -1
```

---

## MCP 서버

Spring AI의 `@Tool` 어노테이션을 사용해 MCP Tool을 3개 구현했다. Claude Desktop이나 MCP를 지원하는 AI 클라이언트에서 바로 kimting의 기억에 접근할 수 있다.

```java
@Tool(description = "사용자의 기억에서 질문과 관련된 내용을 검색한다.")
public String searchMemory(String query) { ... }

@Tool(description = "최근에 저장된 기억 목록을 반환한다.")
public String recentMemories() { ... }

@Tool(description = "특정 사람과 관련된 기억을 검색한다.")
public String memoriesByPerson(String personName) { ... }
```

MCP는 Anthropic이 공개한 오픈 프로토콜이므로 누구나 서버를 구현할 수 있다.

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.5.16 |
| AI 프레임워크 | Spring AI 1.1.0 |
| 임베딩 모델 | ko-sroberta-multitask (ONNX, 로컬 실행) |
| 벡터 DB | pgvector (PostgreSQL 16, Docker, 포트 5433) |

임베딩 모델은 외부 API를 사용하지 않고 ONNX 형식으로 로컬에서 실행한다. **Privacy-first** 원칙 — 개인 기억 데이터를 외부 서버로 보내지 않는다.

---

## 빌드 확인

```
./gradlew compileJava
BUILD SUCCESSFUL
```

---

## 다음 주 (2주차)

`KakaoImporter.parse()` 구현 — 카카오톡 내보내기 `.txt` 파일 파싱.

카카오톡 대화 내보내기는 iOS와 Android가 포맷이 다르다. 두 포맷 모두 지원하고, 대화를 의미 단위로 분절해 Memory로 변환하는 로직을 작성할 예정이다.

---

*소스코드: [github.com/kimting](https://github.com)*
