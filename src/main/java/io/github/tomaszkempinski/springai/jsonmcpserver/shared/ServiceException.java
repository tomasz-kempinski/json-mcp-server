package io.github.tomaszkempinski.springai.jsonmcpserver.shared;

import lombok.Getter;

@Getter
public class ServiceException extends RuntimeException {
  private final ErrorType errorType;
  private final transient Object[] args;

  public ServiceException(ErrorType errorType, String message) {
    super(message);
    this.errorType = errorType;
    this.args = new Object[0];
  }

  public ServiceException(ErrorType errorType, String message, Throwable cause) {
    super(message, cause);
    this.errorType = errorType;
    this.args = new Object[0];
  }

  public ServiceException(ErrorType errorType, String message, Object... args) {
    super(message);
    this.errorType = errorType;
    this.args = args;
  }
}
