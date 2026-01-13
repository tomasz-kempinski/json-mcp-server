package io.github.tomaszkempinski.springai.jsonmcpserver;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.networknt.schema.*;
import com.networknt.schema.Error;
import com.networknt.schema.dialect.Dialects;
import com.saasquatch.jsonschemainferrer.JsonSchemaInferrer;
import com.saasquatch.jsonschemainferrer.SpecVersion;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
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

  @McpTool(
      name = "validate_json_schema",
      description = "Validates that a JSON schema is properly formed")
  public String validateJsonSchema(
      McpSyncRequestContext context,
      @McpToolParam(description = "JSON schema object to validate as a string", required = false)
          String schema,
      @McpToolParam(description = "Path to file containing JSON schema", required = false)
          String schemaFilePath) {

    context.info("Validating JSON schema");

    try {
      String schemaContent;
      if (schema != null && !schema.isBlank()) {
        schemaContent = schema;
      } else if (schemaFilePath != null && !schemaFilePath.isBlank()) {
        File file = new File(schemaFilePath);
        if (!file.exists()) {
          context.error("Schema file does not exist at path: " + schemaFilePath);
          return "Error: Schema file does not exist at path: " + schemaFilePath;
        }
        schemaContent = Files.readString(file.toPath());
      } else {
        context.error("Either 'schema' or 'schemaFilePath' must be provided.");
        return "Error: Either 'schema' or 'schemaFilePath' must be provided.";
      }

      SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012());
      Schema metaSchema =
          schemaRegistry.getSchema(SchemaLocation.of(Dialects.getDraft202012().getId()));
      List<Error> errors =
          metaSchema.validate(
              schemaContent,
              InputFormat.JSON,
              executionContext ->
                  executionContext.executionConfig(
                      executionConfig -> executionConfig.formatAssertionsEnabled(true)));

      if (errors.isEmpty()) {
        context.info("Schema is valid!");
        return "Schema is valid!";
      } else {
        String errorMsgs =
            errors.stream()
                .map(e -> e.getInstanceLocation() + ": " + e.getMessage())
                .collect(Collectors.joining("\n"));
        context.error("Schema validation failed:\n" + errorMsgs);
        return "Schema validation failed:\n" + errorMsgs;
      }
    } catch (IOException | RuntimeException e) {
      context.error("Error reading or parsing JSON schema: " + e.getMessage());
      return "Error reading or parsing JSON schema: " + e.getMessage();
    } catch (Exception e) {
      context.error("Unexpected error during schema validation: " + e.getMessage());
      return "Unexpected error during schema validation: " + e.getMessage();
    }
  }

  @McpTool(
      name = "query_json",
      description =
          """
                Searches a local JSON file using JsonPath query and returns the result as a JSON string

                Parameters:
                - absolutePath: absolute path to the local JSON file
                - query: JsonPath expression

                Examples:
                - $.users[*].name
                - $.orders[?(@.total > 100)]
                - $.users[?(@.active == true && @.age > 30)]
                """)
  public String queryJson(
      McpSyncRequestContext context,
      @McpToolParam(description = "The absolute path to the local JSON file") String absolutePath,
      @McpToolParam(description = "The JsonPath query to execute (e.g. '$.store.book[*].author')")
          String query) {

    context.info("Searching JSON file: " + absolutePath + " with query: " + query);

    try {
      File file = new File(absolutePath);
      if (!file.exists()) {
        context.error("File does not exist at path: " + absolutePath);
        return "Error: File does not exist at path: " + absolutePath;
      }

      Object result = JsonPath.read(file, query);
      return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    } catch (IOException e) {
      context.error("Error reading JSON file: " + e.getMessage());
      return "Error reading JSON file: " + e.getMessage();
    } catch (Exception e) {
      context.error("Error executing JsonPath query: " + e.getMessage());
      return "Error executing JsonPath query: " + e.getMessage();
    }
  }
}
