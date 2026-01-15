package io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.config;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mcp.tools.file")
@Getter
@Setter
public class FileProperties {
  private List<String> allowedExtensions = List.of(".json");
}
