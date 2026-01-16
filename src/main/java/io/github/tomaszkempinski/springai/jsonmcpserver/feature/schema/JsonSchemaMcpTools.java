package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema;

import lombok.RequiredArgsConstructor;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonSchemaMcpTools {

  private final SchemaGenerationService schemaGenerationService;
  private final SchemaValidationService schemaValidationService;

  @McpTool(
      name = "generate_json_schema",
      description = "Generates a JSON schema from a local JSON file")
  public String generateJsonSchema(
      McpSyncRequestContext context,
      @McpToolParam(description = "The absolute path to the local JSON file") String absolutePath) {

    return schemaGenerationService.generateSchema(absolutePath);
  }

  @McpTool(
      name = "validate_json_schema",
      description = "Validates that a JSON schema is properly formed")
  public String validateJsonSchema(
      McpSyncRequestContext context,
      @McpToolParam(description = "JSON schema object to validate as a string", required = false)
          String schema,
      @McpToolParam(description = "Path to file containing JSON schema", required = false)
          String schemaFilePath) {

    String schemaContent = schemaValidationService.loadSchemaContent(schema, schemaFilePath);
    schemaValidationService.validateSchema(schemaContent);
    return "JSON Schema is valid";
  }
}
