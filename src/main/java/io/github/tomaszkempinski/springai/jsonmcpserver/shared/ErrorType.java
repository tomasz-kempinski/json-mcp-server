package io.github.tomaszkempinski.springai.jsonmcpserver.shared;

import lombok.Getter;

@Getter
public enum ErrorType {
  FILE_NOT_FOUND("File not found", "File does not exist at path: {0}"),
  INVALID_INPUT("Invalid input", "Either 'schema' or 'schemaFilePath' must be provided."),
  PROCESSING_ERROR("Processing error", "{0}"),
  VALIDATION_ERROR("Validation error", "Schema validation failed:\n{0}"),
  IO_ERROR("I/O error", "{0}"),
  INVALID_FILE_EXTENSION(
      "Invalid file extension", "File extension is invalid. Allowed: {0}. File: {1}");

  private final String title;
  private final String messagePattern;

  ErrorType(String title, String messagePattern) {
    this.title = title;
    this.messagePattern = messagePattern;
  }
}
