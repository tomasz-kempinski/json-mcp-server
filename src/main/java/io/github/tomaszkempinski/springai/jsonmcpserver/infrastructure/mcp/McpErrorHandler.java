package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.mcp;

import io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema.JsonValidationException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import java.text.MessageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class McpErrorHandler {

  private final McpResponseFormatter mcpResponseFormatter;

  public String handleException(Throwable t, McpSyncRequestContext context) {
    String errorMessage;
    ErrorType errorType;
    Throwable cause = t;

    switch (t) {
      case JsonValidationException jve -> {
        errorMessage = mcpResponseFormatter.formatValidationErrors(jve.getErrors());
        errorType = ErrorType.VALIDATION_ERROR;
      }
      case ServiceException se -> {
        errorType = se.getErrorType();
        errorMessage = formatMessage(errorType.getMessagePattern(), se.getArgs(), se.getMessage());
        if (se.getCause() != null) {
          cause = se.getCause();
        }
      }
      case java.io.IOException _ -> {
        errorMessage = t.getMessage();
        errorType = ErrorType.IO_ERROR;
      }
      default -> {
        errorMessage = t.getMessage();
        errorType = ErrorType.PROCESSING_ERROR;
      }
    }

    return logAndSendError(context, errorType, errorMessage, cause);
  }

  private String logAndSendError(
      McpSyncRequestContext context, ErrorType errorType, String errorMessage, Throwable cause) {
    String fullErrorMessage = "Error [" + errorType + "]: " + errorMessage;

    log.error(fullErrorMessage, cause);
    context.error(fullErrorMessage);

    return fullErrorMessage;
  }

  private String formatMessage(String pattern, Object[] args, String defaultMessage) {
    if (args == null || args.length == 0) {
      return defaultMessage != null ? defaultMessage : pattern;
    }
    try {
      return MessageFormat.format(pattern, args);
    } catch (IllegalArgumentException _) {
      log.warn("Failed to format message with pattern: {}", pattern);
      return defaultMessage;
    }
  }
}
