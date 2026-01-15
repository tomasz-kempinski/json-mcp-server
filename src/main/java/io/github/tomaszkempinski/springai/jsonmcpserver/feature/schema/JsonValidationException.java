package io.github.tomaszkempinski.springai.jsonmcpserver.feature.schema;

import com.networknt.schema.Error;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ErrorType;
import io.github.tomaszkempinski.springai.jsonmcpserver.shared.ServiceException;
import java.util.List;
import lombok.Getter;

@Getter
public class JsonValidationException extends ServiceException {
  private final transient List<Error> errors;

  public JsonValidationException(String message, List<Error> errors) {
    super(ErrorType.VALIDATION_ERROR, message);
    this.errors = errors;
  }
}
