package com.buspass.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** Uniform JSON error body returned for every failure. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(LocalDateTime timestamp,
                       int status,
                       String error,
                       String message,
                       String path,
                       Map<String, String> fieldErrors) {
}
