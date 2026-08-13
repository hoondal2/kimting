package com.kimting.kimting.shared.importer;

import com.kimting.kimting.memory.domain.Memory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface MemoryImporter {

    String getSource();

    List<Memory> parse(InputStream input) throws IOException;
}
