package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.JsonProcessingException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonMapper {

  private final ObjectMapper objectMapper;

  public String toJsonString(Object object) {
    try {
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    } catch (IOException e) {
      throw new JsonProcessingException("Error mapping object to JSON string", e);
    }
  }
}
