package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema;

public interface SchemaValidationService {
  void validateSchema(String schemaContent);

  String loadSchemaContent(String schema, String schemaFilePath);
}
