package com.fwd.is.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.is.common.exceptions.FileIOException;
import com.fwd.is.common.exceptions.JsonParsingException;

public class ParseFileContentTask implements Callable<Object> {

	private static final String ERROR_OCCURED_WHILE_PARSING_JSON = "Error occured while parsing json. payload: %s";
	private static final String ERROR_OCCURED_WHILE_PERFORMING_IO_OPERATION = "Error occured while performing I/O operation on a file. filePath: %s";

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
	public Object call() {
		String output = StringUtils.EMPTY;
		try (InputStream in = getClass().getResourceAsStream(fileLocation);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
			output = readBigStringIn(reader);
		} catch (IOException e) {
			throw new FileIOException(String.format(ERROR_OCCURED_WHILE_PERFORMING_IO_OPERATION, fileLocation), e);
		}

		Object result = null;
		try {
			result = objectMapper.readValue(output, type);
		} catch (JsonProcessingException e) {
			throw new JsonParsingException(String.format(ERROR_OCCURED_WHILE_PARSING_JSON, output), e);
		}
		return result;
	}

	private String readBigStringIn(BufferedReader buffIn) throws IOException {
		StringBuilder everything = new StringBuilder();
		String line;
		while ((line = buffIn.readLine()) != null) {
			everything.append(line);
		}
		return everything.toString();
	}

}
