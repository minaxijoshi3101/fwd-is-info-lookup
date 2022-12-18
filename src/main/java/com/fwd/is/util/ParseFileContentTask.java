package com.fwd.is.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.commons.io.FileUtils;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseFileContentTask implements Callable<Object> {

	private String fileLocation;
	private TypeReference<?> type;
	private ObjectMapper objectMapper;

	public ParseFileContentTask() {
		super();
	}

	public ParseFileContentTask(String fileLocation, TypeReference<?> type, ObjectMapper objectMapper) {
		super();
		this.fileLocation = fileLocation;
		this.type = type;
		this.objectMapper = objectMapper;
	}

	@Override
	public Object call() throws Exception {
		File file = ResourceUtils.getFile(fileLocation);
		String fileContent = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
		return objectMapper.readValue(fileContent, type);
	}

}
