package io.github.tomaszkempinski.springai.jsonmcpserver.feature.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.query.internal.JsonQueryServiceImpl;
import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.json.JsonMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.context.McpSyncRequestContext;

class JsonQueryMcpToolsTest {

  private JsonQueryMcpTools tool;
  private McpSyncRequestContext context;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonMapper jsonMapper = new JsonMapper(objectMapper);
    FileValidationService fileValidationService = mock(FileValidationService.class);

    when(fileValidationService.validateFileExists(anyString()))
        .thenAnswer(
            invocation -> {
              String path = invocation.getArgument(0);
              if (path.endsWith(".xml")) {
                throw new ServiceException(
                    ErrorType.INVALID_FILE_EXTENSION, "File extension is invalid");
              }
              return Paths.get(path);
            });

    JsonQueryService jsonQueryService = new JsonQueryServiceImpl(jsonMapper, fileValidationService);

    tool = new JsonQueryMcpTools(jsonQueryService);
    context = mock(McpSyncRequestContext.class);
  }

  @Test
  void testQueryJson() throws IOException {
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
          toMcpResponse(
              () ->
                  tool.queryJson(
                      context, tempFile.getAbsolutePath(), "$[?(@.color == 'red')].name"));
      System.out.println("Search Result:\n" + result);
      assertTrue(result.contains("Apple"));
      assertTrue(result.contains("Cherry"));
      assertFalse(result.contains("Banana"));
    } finally {
      tempFile.delete();
    }
  }

  @Test
  void testQueryJsonInvalidExtension() {
    String result = toMcpResponse(() -> tool.queryJson(context, "data.xml", "$.name"));
    assertTrue(result.contains("Error [INVALID_FILE_EXTENSION]"));
    assertTrue(result.contains("File extension is invalid"));
  }

  private String toMcpResponse(Supplier<String> supplier) {
    try {
      return supplier.get();
    } catch (ServiceException e) {
      return "Error [" + e.getErrorType() + "]: " + e.getMessage();
    } catch (Exception e) {
      return "Error [PROCESSING_ERROR]: " + e.getMessage();
    }
  }
}
