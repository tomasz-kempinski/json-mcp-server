package io.github.tomaszkempinski.springai.jsonmcpserver.shared;

public class JsonProcessingException extends ServiceException {

  public JsonProcessingException(String message, Throwable cause) {
    super(ErrorType.PROCESSING_ERROR, message, cause);
  }
}
