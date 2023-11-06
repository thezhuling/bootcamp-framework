package org.github.bootcamp.tooltik;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import java.io.IOException;
import java.io.Serial;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings({"unused"})
@Slf4j
public class JsonUtil {
  private static final String STD_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final String DATE_PATTERN = "yyyy-MM-dd";
  private static final String TIME_PATTERN = "HH:mm:ss";
  private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern(DATE_PATTERN);
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern(TIME_PATTERN);
  private static final DateTimeFormatter DAY_TIME_FMT = DateTimeFormatter.ofPattern(STD_PATTERN);

  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.setDateFormat(new SimpleDateFormat(STD_PATTERN));

    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DAY_FMT));
    javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(TIME_FMT));
    javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DAY_TIME_FMT));

    javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DAY_FMT));
    javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(TIME_FMT));
    javaTimeModule.addDeserializer(
        LocalDateTime.class, new LocalDateTimeDeserializer(DAY_TIME_FMT));
    OBJECT_MAPPER.registerModule(javaTimeModule);

    StringTrimModule stringTrimModule = new StringTrimModule();
    OBJECT_MAPPER.registerModule(stringTrimModule);

    OBJECT_MAPPER.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
    OBJECT_MAPPER.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    OBJECT_MAPPER.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
  }

  public static String toJson(Object object) {
    String result = StringUtils.EMPTY;
    if (Objects.isNull(object)) {
      return result;
    }

    try {
      result = OBJECT_MAPPER.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      log.error("jackson object to string error", e);
    }

    return result;
  }

  public static <T> T parseObject(String text, Class<T> clazz) {
    if (StringUtils.isBlank(text)) {
      return null;
    }

    T result = null;
    try {
      result = OBJECT_MAPPER.readValue(text, clazz);
    } catch (JsonProcessingException e) {
      log.error("jackson string to object error", e);
    }

    return result;
  }

  public static <T> List<T> parseArray(String text, Class<T> clazz) {
    if (StringUtils.isBlank(text)) {
      return null;
    }
    List<T> result = null;
    try {
      CollectionLikeType collectionLikeType =
          OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, clazz);
      result = OBJECT_MAPPER.readValue(text, collectionLikeType);
    } catch (Exception e) {
      log.error("jackson string to array error", e);
    }
    return result;
  }

  public static JsonNode parseJsonNode(String text) {
    JsonNode result = null;
    if (StringUtils.isBlank(text)) {
      return null;
    }

    try {
      result = OBJECT_MAPPER.readTree(text);
    } catch (JsonProcessingException e) {
      log.error("jackson parse json node error", e);
    }

    return result;
  }

  public static String getString(JsonNode jsonNode, String key) {
    String result = StringUtils.EMPTY;
    if (jsonNode == null) {
      return result;
    }

    JsonNode objNode = jsonNode.get(key);
    if (objNode.isValueNode()) {
      result = objNode.asText();
    }

    return result;
  }

  public static Object get(JsonNode jsonNode, String key) {
    Object result;
    if (jsonNode == null) {
      return null;
    }

    JsonNode objNode = jsonNode.get(key);
    if (objNode.isValueNode()) {
      result = objNode.asText();
    } else if (objNode.isInt()) {
      result = objNode.asInt();
    } else if (objNode.isDouble()) {
      result = objNode.asDouble();
    } else if (objNode.isBoolean()) {
      result = objNode.asBoolean();
    } else if (objNode.isFloat()) {
      result = objNode.floatValue();
    } else if (objNode.isBigDecimal()) {
      result = objNode.decimalValue();
    } else if (objNode.isBigInteger()) {
      result = objNode.bigIntegerValue();
    } else {
      result = toJson(objNode);
    }

    return result;
  }

  public static class StringTrimModule extends SimpleModule {
    @Serial private static final long serialVersionUID = 1L;

    public StringTrimModule() {
      addDeserializer(
          String.class,
          new StdScalarDeserializer<>(String.class) {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            public String deserialize(
                JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
              String value = jsonParser.getValueAsString();
              return value.trim();
            }
          });
    }
  }
}
