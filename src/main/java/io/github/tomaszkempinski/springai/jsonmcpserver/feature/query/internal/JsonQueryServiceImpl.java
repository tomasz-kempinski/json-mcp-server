package io.github.tomaszkempinski.springai.jsonmcpserver.feature.query.internal;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import io.github.tomaszkempinski.springai.jsonmcpserver.feature.query.JsonQueryService;
import io.github.tomaszkempinski.springai.jsonmcpserver.infrastructure.json.JsonMapper;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.file.FileValidationService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JsonQueryServiceImpl implements JsonQueryService {

  private final JsonMapper jsonMapper;
  private final FileValidationService fileValidationService;

  @Override
  public String query(String absolutePath, String query) {
    Path path = fileValidationService.validateFileExists(absolutePath);
    try (var is = Files.newInputStream(path)) {
      Object result = JsonPath.read(is, query);
      return jsonMapper.toJsonString(result);
    } catch (IOException e) {
      throw new ServiceException(ErrorType.IO_ERROR, e.getMessage(), e);
    } catch (JsonPathException e) {
      throw new ServiceException(
          ErrorType.PROCESSING_ERROR, "Executing JsonPath query: " + e.getMessage(), e);
    }
  }
}
