package com.kimting.kimting.importer;

import com.kimting.kimting.core.domain.Memory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface MemoryImporter {

    String getSource();

    List<Memory> parse(InputStream input) throws IOException;
}
