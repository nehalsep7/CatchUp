/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.squareup.moshi;

import android.support.v4.util.ArrayMap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Converts maps with string keys to JSON objects.
 */
public final class ArrayMapJsonAdapter<K, V> extends JsonAdapter<Map<K, V>> {
  public static final Factory FACTORY = (type, annotations, moshi) -> {
    if (!annotations.isEmpty()) return null;
    Class<?> rawType = Types.getRawType(type);
    if (rawType != Map.class) return null;
    Type[] keyAndValue = Types.mapKeyAndValueTypes(type, rawType);
    return new ArrayMapJsonAdapter<>(moshi, keyAndValue[0], keyAndValue[1]).nullSafe();
  };

  private final JsonAdapter<K> keyAdapter;
  private final JsonAdapter<V> valueAdapter;

  public ArrayMapJsonAdapter(Moshi moshi, Type keyType, Type valueType) {
    this.keyAdapter = moshi.adapter(keyType);
    this.valueAdapter = moshi.adapter(valueType);
  }

  @Override public void toJson(JsonWriter writer, Map<K, V> map) throws IOException {
    writer.beginObject();
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (entry.getKey() == null) {
        throw new JsonDataException("Map key is null at path " + writer.getPath());
      }
      writer.promoteNameToValue();
      keyAdapter.toJson(writer, entry.getKey());
      valueAdapter.toJson(writer, entry.getValue());
    }
    writer.endObject();
  }

  @Override public Map<K, V> fromJson(JsonReader reader) throws IOException {
    ArrayMap<K, V> result = new ArrayMap<>();
    reader.beginObject();
    while (reader.hasNext()) {
      reader.promoteNameToValue();
      K name = keyAdapter.fromJson(reader);
      V value = valueAdapter.fromJson(reader);
      V replaced = result.put(name, value);
      if (replaced != null) {
        throw new JsonDataException("Map key '" + name + "' has multiple values at path "
            + reader.getPath());
      }
    }
    reader.endObject();
    return result;
  }

  @Override public String toString() {
    return "JsonAdapter(" + keyAdapter + "=" + valueAdapter + ")";
  }
}