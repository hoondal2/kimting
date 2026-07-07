package com.kimting.kimting.importer;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.core.domain.MemoryType;
import com.kimting.kimting.importer.kakao.KakaoImporter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoImporterTest {

    private KakaoImporter importer;

    @BeforeEach
    void setUp() {
        importer = new KakaoImporter();
    }

    @Test
    @DisplayName("Android 포맷 파싱 - 기본 메시지")
    void parseAndroidFormat() throws IOException {
        String chat = """
            카카오톡 대화
            대화상대: 창수
            저장한 날짜 : 2024년 1월 15일 오후 3:45

            2024년 1월 15일 월요일
            [창수] [오전 10:30] 야 오늘 점심 같이 먹을래?
            [나] [오전 10:31] ㅇㅋ 12시 학교 앞 어때?
            [창수] [오전 10:32] 좋아
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories).hasSize(1);
        Memory m = memories.get(0);
        assertThat(m.getType()).isEqualTo(MemoryType.CONVERSATION);
        assertThat(m.getPeople()).contains("창수", "나");
        assertThat(m.getContent()).contains("점심");
        assertThat(m.getSource()).isEqualTo("kakao");
        assertThat(m.getOccurredAt().getHour()).isEqualTo(10);
        assertThat(m.getOccurredAt().getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("iOS 포맷 파싱 - 기본 메시지")
    void parseIosFormat() throws IOException {
        String chat = """
            창수님과 카카오톡 대화
            저장한 날짜 : 2024-01-15

            2024년 1월 15일 월요일
            오전 10:30, 창수 : 야 오늘 점심 같이 먹을래?
            오전 10:31, 나 : ㅇㅋ 12시 학교 앞 어때?
            오전 10:32, 창수 : 좋아
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).getPeople()).contains("창수", "나");
    }

    @Test
    @DisplayName("30분 이상 공백이면 세션을 분리한다")
    void splitSessionByGap() throws IOException {
        String chat = """
            2024년 1월 15일 월요일
            [창수] [오전 10:00] 첫 번째 대화
            [나] [오전 10:05] 응
            [창수] [오후 3:00] 두 번째 대화 (2시간 50분 후)
            [나] [오후 3:01] 응
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories).hasSize(2);
    }

    @Test
    @DisplayName("30분 이내 메시지는 하나의 세션으로 묶는다")
    void groupMessagesWithinSession() throws IOException {
        String chat = """
            2024년 1월 15일 월요일
            [창수] [오전 10:00] 안녕
            [나] [오전 10:10] 안녕
            [창수] [오전 10:25] 점심 먹었어?
            [나] [오전 10:28] 아직
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).getContent()).contains("안녕", "점심");
    }

    @Test
    @DisplayName("오후 시간을 올바르게 변환한다")
    void parseAfternoonTime() throws IOException {
        String chat = """
            2024년 1월 15일 월요일
            [창수] [오후 3:30] 오후 대화
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories.get(0).getOccurredAt().getHour()).isEqualTo(15);
        assertThat(memories.get(0).getOccurredAt().getMinute()).isEqualTo(30);
    }

    @Test
    @DisplayName("시스템 메시지는 무시한다")
    void ignoreSystemMessages() throws IOException {
        String chat = """
            2024년 1월 15일 월요일
            창수님이 들어왔습니다.
            [창수] [오전 10:00] 안녕
            [나] [오전 10:01] 응
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories).hasSize(1);
        assertThat(memories.get(0).getContent()).doesNotContain("들어왔습니다");
    }

    @Test
    @DisplayName("멀티라인 메시지를 하나로 합친다")
    void mergeMultilineMessage() throws IOException {
        String chat = """
            2024년 1월 15일 월요일
            [창수] [오전 10:00] 첫 줄
            두 번째 줄
            세 번째 줄
            [나] [오전 10:01] 응
            """;

        List<Memory> memories = importer.parse(toStream(chat));

        assertThat(memories.get(0).getContent()).contains("첫 줄", "두 번째 줄", "세 번째 줄");
    }

    private InputStream toStream(String text) {
        return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
    }
}
