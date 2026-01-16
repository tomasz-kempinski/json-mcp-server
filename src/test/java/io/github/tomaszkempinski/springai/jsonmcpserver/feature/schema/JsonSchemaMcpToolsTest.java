package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.internal.SchemaGenerationServiceImpl;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.internal.SchemaValidationServiceImpl;
import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.json.JsonMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.mcp.McpResponseFormatter;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.context.McpSyncRequestContext;

class JsonSchemaMcpToolsTest {

  private JsonSchemaMcpTools tool;
  private McpSyncRequestContext context;
  private McpResponseFormatter mcpResponseFormatter;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonSchemaInferrer inferrer =
        JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_2020_12).build();
    SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
    Schema metaSchema =
        schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().getId()));

    JsonMapper jsonMapper = new JsonMapper(objectMapper);
    FileValidationService fileValidationService = mock(FileValidationService.class);
    mcpResponseFormatter = new McpResponseFormatter();

    when(fileValidationService.validateFileExists(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              if (path.contains("non_existent_file")) {
                throw new ServiceException(ErrorType.FILE_NOT_FOUND, "File does not exist");
              }
              if (path.endsWith(".txt")) {
                throw new ServiceException(
                    ErrorType.INVALID_FILE_EXTENSION, "File extension is invalid");
              }
              return Paths.get(path);
            });

    SchemaGenerationService schemaGenerationService =
        new SchemaGenerationServiceImpl(inferrer, objectMapper, jsonMapper, fileValidationService);
    SchemaValidationService schemaValidationService =
        new SchemaValidationServiceImpl(metaSchema, fileValidationService);

    tool = new JsonSchemaMcpTools(schemaGenerationService, schemaValidationService);
    context = mock(McpSyncRequestContext.class);
  }

  @Test
  void testGenerateJsonSchema() throws IOException {
    File tempFile = File.createTempFile("test", ".json");
    String content = "{\"name\": \"test\", \"age\": 30, \"active\": true}";
    Files.writeString(tempFile.toPath(), content);

    try {
      String schema =
          toMcpResponse(() -> tool.generateJsonSchema(context, tempFile.getAbsolutePath()));
      System.out.println("Generated Schema:\n" + schema);
      assertTrue(schema.contains("\"type\" : \"object\""));
      assertTrue(schema.contains("\"name\""));
      assertTrue(schema.contains("\"age\""));
      assertTrue(schema.contains("\"active\""));
    } finally {
      tempFile.delete();
    }
  }

  @Test
  void testGenerateJsonSchemaFileNotFound() {
    String result = toMcpResponse(() -> tool.generateJsonSchema(context, "non_existent_file.json"));
    assertTrue(result.contains("Error [FILE_NOT_FOUND]"));
    assertTrue(result.contains("File does not exist"));
  }

  @Test
  void testGenerateJsonSchemaInvalidExtension() {
    String result = toMcpResponse(() -> tool.generateJsonSchema(context, "test.txt"));
    assertTrue(result.contains("Error [INVALID_FILE_EXTENSION]"));
    assertTrue(result.contains("File extension is invalid"));
  }

  @Test
  void testValidateJsonSchemaSuccess() {
    String schema =
        """
            {
              "$schema": "https://json-schema.org/draft/2020-12/schema",
              "type": "object",
              "properties": {
                "name": { "type": "string" }
              }
            }
            """;

    String result = toMcpResponse(() -> tool.validateJsonSchema(context, schema, null));
    assertTrue(result.contains("Schema is valid"));
  }

  @Test
  void testValidateJsonSchemaInvalid() {
    // Invalid schema: "type" should be a string or array, not a number
    String schema =
        """
            {
              "type": 123
            }
            """;

    String result = toMcpResponse(() -> tool.validateJsonSchema(context, schema, null));
    assertTrue(result.contains("Schema validation failed") || result.contains("Error"));
  }

  @Test
  void testValidateJsonSchemaFromFile() throws IOException {
    File tempFile = File.createTempFile("schema", ".json");
    String schema = "{\"type\": \"object\"}";
    Files.writeString(tempFile.toPath(), schema);

    try {
      String result =
          toMcpResponse(() -> tool.validateJsonSchema(context, null, tempFile.getAbsolutePath()));
      assertTrue(result.contains("Schema is valid"));
    } finally {
      tempFile.delete();
    }
  }

  @Test
  void testValidateJsonSchemaNoParams() {
    String result = toMcpResponse(() -> tool.validateJsonSchema(context, null, null));
    assertTrue(result.contains("Error"));
    assertTrue(result.contains("must be provided"));
  }

  @Test
  void testValidateJsonSchemaInvalidExtension() {
    String result = toMcpResponse(() -> tool.validateJsonSchema(context, null, "schema.txt"));
    assertTrue(result.contains("Error [INVALID_FILE_EXTENSION]"));
    assertTrue(result.contains("File extension is invalid"));
  }

  @Test
  void testValidateJsonSchemaBrokenJson() {
    String result = toMcpResponse(() -> tool.validateJsonSchema(context, "{ invalid json }", null));
    assertTrue(result.contains("Reading or parsing JSON schema"));
  }

  private String toMcpResponse(Supplier<String> supplier) {
    try {
      return supplier.get();
    } catch (JsonValidationException e) {
      return "Error [VALIDATION_ERROR]: "
          + mcpResponseFormatter.formatValidationErrors(e.getErrors());
    } catch (ServiceException e) {
      String message = MessageFormat.format(e.getErrorType().getMessagePattern(), e.getArgs());
      return "Error [" + e.getErrorType() + "]: " + message;
    } catch (Exception e) {
      return "Error [PROCESSING_ERROR]: " + e.getMessage();
    }
  }
}
