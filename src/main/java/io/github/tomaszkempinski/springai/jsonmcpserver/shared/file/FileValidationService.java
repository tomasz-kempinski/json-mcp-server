package io.github.tomaszkempinski.springai.jsonmcpserver.shared.file;

import java.nio.file.Path;

public interface FileValidationService {
  Path validateFileExists(String absolutePath);
}
