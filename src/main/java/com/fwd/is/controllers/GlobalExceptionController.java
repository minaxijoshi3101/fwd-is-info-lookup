package com.fwd.is.controllers;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionController {

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<Object> handleMethodNotAllowedException(HttpServletRequest request, Exception e) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("message", "Method not allowed.");
		return new ResponseEntity<>(body, HttpStatus.METHOD_NOT_ALLOWED);
	}
}