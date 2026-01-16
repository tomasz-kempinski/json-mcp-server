package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.SchemaGenerationService;
import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.json.JsonMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaGenerationServiceImpl implements SchemaGenerationService {

  private final JsonSchemaInferrer inferrer;
  private final ObjectMapper objectMapper;
  private final JsonMapper jsonMapper;
  private final FileValidationService fileValidationService;

  @Override
  public String generateSchema(String absolutePath) {
    Path path = fileValidationService.validateFileExists(absolutePath);
    try (var is = Files.newInputStream(path)) {
      JsonNode jsonNode = objectMapper.readTree(is);
      JsonNode schema = inferrer.inferForSample(jsonNode);
      return jsonMapper.toJsonString(schema);
    } catch (IOException e) {
      throw new ServiceException(
          ErrorType.PROCESSING_ERROR, "Reading or processing JSON file: " + e.getMessage(), e);
    }
  }
}
