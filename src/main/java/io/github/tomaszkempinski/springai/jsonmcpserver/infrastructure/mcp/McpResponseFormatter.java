package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.mcp;

import com.networknt.schema.Error;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class McpResponseFormatter {

  public String formatValidationErrors(List<Error> errors) {
    return errors.stream()
        .map(e -> e.getInstanceLocation() + ": " + e.getMessage())
        .collect(Collectors.joining("\n"));
  }
}
