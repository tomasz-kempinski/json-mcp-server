package io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.internal;

import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.config.FileProperties;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileValidationServiceImpl implements FileValidationService {

  private final FileProperties fileProperties;

  @Override
  public Path validateFileExists(String absolutePath) {
    if (absolutePath == null || !isValidExtension(absolutePath)) {
      throw new ServiceException(
          ErrorType.INVALID_FILE_EXTENSION,
          "Invalid file extension",
          String.join(", ", fileProperties.getAllowedExtensions()),
          absolutePath);
    }

    Path path = Paths.get(absolutePath);
    if (!Files.exists(path)) {
      throw new ServiceException(ErrorType.FILE_NOT_FOUND, "File not found", absolutePath);
    }
    return path;
  }

  private boolean isValidExtension(String path) {
    String lowerPath = path.toLowerCase();
    return fileProperties.getAllowedExtensions().stream()
        .anyMatch(ext -> lowerPath.endsWith(ext.toLowerCase()));
  }
}
