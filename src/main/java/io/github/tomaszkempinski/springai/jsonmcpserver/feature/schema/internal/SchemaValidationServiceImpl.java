package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.internal;

import com.networknt.schema.Error;
import com.networknt.schema.InputFormat;
import com.networknt.schema.Schema;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.JsonValidationException;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.SchemaValidationService;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchemaValidationServiceImpl implements SchemaValidationService {

  private final Schema metaSchema;
  private final FileValidationService fileValidationService;

  @Override
  public void validateSchema(String schemaContent) {
    try {
      List<Error> errors =
          metaSchema.validate(
              schemaContent,
              InputFormat.JSON,
              executionContext ->
                  executionContext.executionConfig(
                      executionConfig -> executionConfig.formatAssertionsEnabled(true)));

      if (!errors.isEmpty()) {
        throw new JsonValidationException("JSON schema validation failed", errors);
      }
    } catch (JsonValidationException e) {
      throw e;
    } catch (Exception e) {
      throw new ServiceException(
          ErrorType.PROCESSING_ERROR,
          "Reading or parsing JSON schema: " + e.getMessage(),
          "Reading or parsing JSON schema: " + e.getMessage());
    }
  }

  @Override
  public String loadSchemaContent(String schema, String schemaFilePath) {
    if (schema != null && !schema.isBlank()) {
      return schema;
    } else if (schemaFilePath != null && !schemaFilePath.isBlank()) {
      Path path = fileValidationService.validateFileExists(schemaFilePath);
      return readFileContent(path);
    } else {
      throw new ServiceException(ErrorType.INVALID_INPUT, "Invalid input");
    }
  }

  private String readFileContent(Path path) {
    try {
      return Files.readString(path);
    } catch (IOException e) {
      throw new ServiceException(ErrorType.IO_ERROR, "Reading schema file: " + e.getMessage(), e);
    }
  }
}
