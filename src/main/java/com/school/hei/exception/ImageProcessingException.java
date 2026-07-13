package com.school.hei.exception;

import org.springframework.http.HttpStatus;

public class ImageProcessingException extends ApiException {
  public ImageProcessingException(String message, Throwable cause) {
    super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
  }
}
