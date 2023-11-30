package com.seungju.quiz.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public void handleException(Exception ex) throws Exception {

		ex.printStackTrace();
		throw ex;
	}

}
