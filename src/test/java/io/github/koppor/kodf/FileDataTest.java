package io.github.koppor.kodf;

import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.hash.HashCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileDataTest {

    @Test
    void hashValue() throws Exception {
      Path tempFile = Files.createTempFile("kodf", "tmp");
      FileData fileData = new FileData(tempFile);
      assertEquals(HashCode.fromInt(0), fileData.hashValue());
    }
}
