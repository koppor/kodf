package io.github.koppor.kodf.typeadapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.nio.file.attribute.FileTime;
import java.time.Instant;

public class FileTimeTypeAdapter implements JsonSerializer<FileTime>, JsonDeserializer<FileTime> {

  @Override
  public JsonElement serialize(FileTime src, Type typeOfSrc, JsonSerializationContext context) {
    return new JsonPrimitive(src.toString());
  }

  @Override
  public FileTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return FileTime.from(Instant.parse(json.getAsString()));
  }
}
