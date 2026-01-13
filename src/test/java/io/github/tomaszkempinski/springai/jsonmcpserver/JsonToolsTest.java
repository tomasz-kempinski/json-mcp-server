package io.github.tomaszkempinski.springai.jsonmcpserver;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.context.McpSyncRequestContext;

class JsonToolsTest {

  @Test
  void testGenerateJsonSchema() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    File tempFile = File.createTempFile("test", ".json");
    String content = "{\"name\": \"test\", \"age\": 30, \"active\": true}";
    Files.writeString(tempFile.toPath(), content);

    try {
      String schema = tool.generateJsonSchema(context, tempFile.getAbsolutePath());
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
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    String result = tool.generateJsonSchema(context, "non_existent_file.json");
    assertTrue(result.contains("Error: File does not exist"));
  }

  @Test
  void testValidateJsonSchemaSuccess() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

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

    String result = tool.validateJsonSchema(context, schema, null);
    assertTrue(result.contains("Schema is valid"));
  }

  @Test
  void testValidateJsonSchemaInvalid() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    // Invalid schema: "type" should be a string or array, not a number
    String schema =
        """
            {
              "type": 123
            }
            """;

    String result = tool.validateJsonSchema(context, schema, null);
    assertTrue(result.contains("Schema validation failed") || result.contains("Error"));
  }

  @Test
  void testValidateJsonSchemaFromFile() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    File tempFile = File.createTempFile("schema", ".json");
    String schema = "{\"type\": \"object\"}";
    Files.writeString(tempFile.toPath(), schema);

    try {
      String result = tool.validateJsonSchema(context, null, tempFile.getAbsolutePath());
      assertTrue(result.contains("Schema is valid"));
    } finally {
      tempFile.delete();
    }
  }

  @Test
  void testValidateJsonSchemaNoParams() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    String result = tool.validateJsonSchema(context, null, null);
    assertTrue(result.contains("Error"));
    assertTrue(result.contains("must be provided"));
  }

  @Test
  void testValidateJsonSchemaBrokenJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    String result = tool.validateJsonSchema(context, "{ invalid json }", null);
    assertTrue(result.contains("Error reading or parsing JSON schema"));
  }

  @Test
  void testQueryJson() throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonTools tool = new JsonTools(objectMapper);
    McpSyncRequestContext context = mock(McpSyncRequestContext.class);

    File tempFile = File.createTempFile("test_search", ".json");
    String content =
        """
            [
              {"id": 1, "name": "Apple", "color": "red"},
              {"id": 2, "name": "Banana", "color": "yellow"},
              {"id": 3, "name": "Cherry", "color": "red"}
            ]
            """;
    Files.writeString(tempFile.toPath(), content);

    try {
      // Search for names of red fruits
      String result =
          tool.queryJson(context, tempFile.getAbsolutePath(), "$[?(@.color == 'red')].name");
      System.out.println("Search Result:\n" + result);
      assertTrue(result.contains("Apple"));
      assertTrue(result.contains("Cherry"));
      assertFalse(result.contains("Banana"));
    } finally {
      tempFile.delete();
    }
  }
}
