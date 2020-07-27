package io.github.koppor.kodf.database;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.koppor.kodf.typeadapters.FileTimeTypeAdapter;
import io.github.koppor.kodf.typeadapters.PathTypeAdapter;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import org.junit.jupiter.api.Test;
import org.tinylog.Logger;

class FileDataTest {

  @Test
  void fileDataCanBeSerialized() {
    FileData test = FileData.builder().path(Path.of("test")).size(0L).build();
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(Path.class, new PathTypeAdapter())
            .registerTypeAdapter(FileTime.class, new FileTimeTypeAdapter())
            .setPrettyPrinting()
            .create();
    String json = gson.toJson(test);
    Logger.debug(json);
  }
}
