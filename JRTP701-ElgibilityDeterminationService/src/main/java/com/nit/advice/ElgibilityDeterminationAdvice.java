package com.nit.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ElgibilityDeterminationAdvice {
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> handleAllException(Exception e) {
		return new ResponseEntity<String>(e.getMessage(), HttpStatus.OK);
	}

}
