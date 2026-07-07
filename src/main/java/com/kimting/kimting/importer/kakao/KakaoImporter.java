package com.kimting.kimting.importer.kakao;

import com.kimting.kimting.core.domain.Memory;
import com.kimting.kimting.importer.MemoryImporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class KakaoImporter implements MemoryImporter {

    @Override
    public String getSource() {
        return "kakao";
    }

    @Override
    public List<Memory> parse(InputStream input) throws IOException {
        // TODO: 2주차 구현
        // 카카오톡 내보내기 .txt 파일 파싱
        // iOS / Android 포맷 모두 지원 예정
        throw new UnsupportedOperationException("KakaoImporter: 2주차에 구현 예정");
    }
}
