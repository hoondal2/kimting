package com.kimting.kimting.parser;

public interface MemoryParser {

    /**
     * 자연어 텍스트를 분석해 구조화된 ParseResult를 반환한다.
     * 저장하지 않는다 — 저장은 confirm 단계에서 수행된다.
     */
    ParseResult parse(String text);
}
