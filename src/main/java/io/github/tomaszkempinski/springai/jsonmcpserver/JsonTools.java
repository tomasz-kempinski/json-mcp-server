package io.github.tomaszkempinski.springai.jsonmcpserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import java.io.File;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonTools {

  private final ObjectMapper objectMapper;

  @McpTool(
      name = "generate_json_schema",
      description = "Generates a JSON schema from a local JSON file")
  public String generateJsonSchema(
      McpSyncRequestContext context,
      @McpToolParam(description = "The absolute path to the local JSON file") String absolutePath) {

    context.info("Generating JSON schema for file: " + absolutePath);

    try {
      File file = new File(absolutePath);
      if (!file.exists()) {
        context.error("File does not exist at path: " + absolutePath);
        return "Error: File does not exist at path: " + absolutePath;
      }
      JsonNode jsonNode = objectMapper.readTree(file);
      JsonSchemaInferrer inferrer =
          JsonSchemaInferrer.newBuilder().setSpecVersion(SpecVersion.DRAFT_2020_12).build();
      JsonNode schema = inferrer.inferForSample(jsonNode);
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(schema);
    } catch (IOException e) {
      context.error("Error reading or processing JSON file: " + e.getMessage());
      return "Error reading or processing JSON file: " + e.getMessage();
    }
  }
}
