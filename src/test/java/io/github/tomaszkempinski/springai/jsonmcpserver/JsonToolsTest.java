package io.github.tomaszkempinski.springai.jsonmcpserver;

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
}
