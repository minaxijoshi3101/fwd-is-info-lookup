package com.fwd.is;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.SQLException;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.fwd.ia.clientf.exceptions.ClientFAdaptorException;
import com.fwd.ia.common.exception.IntegrationAdaptorException;
import com.fwd.ia.common.exception.NotFound;
import com.fwd.ia.common.exception.RequestFailedException;
import com.fwd.ia.ebao.adaptor.exceptions.EBAOAdaptorException;
import com.fwd.is.common.controllers.GlobalExceptionController;
import com.fwd.is.common.exceptions.IntegrationServiceException;
import com.fwd.is.common.exceptions.InvalidRequestPayload;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.exceptions.ParallelExecutionException;
import com.fwd.is.common.exceptions.SubmitApplicationException;
import com.fwd.is.common.exceptions.VerificationException;

@SpringBootTest(classes = InfoLookupStarter.class)
@TestInstance(Lifecycle.PER_CLASS)
class GlobalExceptionControllerTest {

	@Autowired
	private GlobalExceptionController globalExceptionController;

	@Test
	@DisplayName("InvalidRequestPayload exception handler test.")
	void invalidRequestPayloadHandlerTest() {
		InvalidRequestPayload exception = new InvalidRequestPayload("Bad Request");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleRequestFailedException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("RequestFailedException exception handler test.")
	void requestFailedExceptionHandlerTest() {
		JSONObject responseJson = new JSONObject();
		responseJson.put("message", "Invalid data");
		RequestFailedException exception = new RequestFailedException("400", "Bad Request", responseJson.toString());
		ResponseEntity<Object> responseEntity = globalExceptionController.handleRequestFailedException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("JsonProcessingException exception handler test.")
	void jsonProcessingExceptionHandlerTest() {
		JsonParsingException exception = new JsonParsingException("Error occured while parsing json.");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleJsonParsingException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("VerificationException exception handler test.")
	void verificationExceptionHandlerTest() {
		VerificationException exception = new VerificationException("Verification error!", "");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleVerificationException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("ClientFAdaptorException exception handler test.")
	void clientFAdaptorExceptionHandlerTest() {
		ClientFAdaptorException exception = new ClientFAdaptorException("ClientF check fail.");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleProcessFailedException(null,
				exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("SQLServerException exception handler test.")
	void sqlServerExceptionHandlerTest() {
		SQLException exception = new SQLException("Error occured while connectiong to DB.");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleSQLException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("EBAOAdaptorException exception handler test when status code is 404.")
	void ebaoAdaptorExceptionHandler404Test() {
		EBAOAdaptorException exception = new EBAOAdaptorException("Error occured.", 404, StringUtils.EMPTY);
		ResponseEntity<Object> responseEntity = globalExceptionController.handleEbaoException(null, exception);
		assertEquals(404, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("EBAOAdaptorException exception handler test when status code is 400.")
	void ebaoAdaptorExceptionHandler400Test() {
		JSONObject responseJson = new JSONObject();
		JSONArray messages = new JSONArray();
		messages.put(Collections.EMPTY_LIST);
		messages.put(Collections.EMPTY_LIST);
		messages.put(Collections.EMPTY_LIST);
		responseJson.put("Messages", messages);
		EBAOAdaptorException exception = new EBAOAdaptorException("Error occured.", 400, responseJson.toString());
		ResponseEntity<Object> responseEntity = globalExceptionController.handleEbaoException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("EBAOAdaptorException exception handler test when status code is 400.")
	void ebaoAdaptorExceptionHandler400AndWithoutEbaoStatusCodeTest() {
		JSONObject responseJson = new JSONObject();
		responseJson.put("message", "Invalid data");
		responseJson.put("status_code", "3");
		EBAOAdaptorException exception = new EBAOAdaptorException("Error occured.", 400, responseJson.toString());
		ResponseEntity<Object> responseEntity = globalExceptionController.handleEbaoException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("RestClientException exception handler test.")
	void restClientExceptionHandlerTest() {
		RestClientException exception = new RestClientException("Something went wrong.");
		ResponseEntity<Object> responseEntity = globalExceptionController.handleResourceAccessException(null,
				exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("ParallelExecutionException exception handler test.")
	void parallelExecutionExceptionHandlerTest() {
		ResourceAccessException ebaoException = new ResourceAccessException("Something went wrong.");
		ParallelExecutionException exception = new ParallelExecutionException("parallel execution exception!",
				new ExecutionException(ebaoException));
		ResponseEntity<Object> responseEntity = globalExceptionController.handleExecutionException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("ParallelExecutionException exception handler without stacktrace test.")
	void parallelExecutionExceptionHandlerWithoutStacktraceTest() {
		ResponseEntity<Object> responseEntity = globalExceptionController.handleExecutionException(null,
				new IntegrationAdaptorException("Error occured."));
		assertEquals(500, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("NotFound exception handler test.")
	void notFoundHandlerTest() {
		JSONObject responseJson = new JSONObject();
		responseJson.put("message", "Resource Not Found");
		NotFound exception = new NotFound("Resource Not Found.", "404", "test", responseJson.toString());
		ResponseEntity<Object> responseEntity = globalExceptionController.handleNotFoundException(null, exception);
		assertEquals(400, responseEntity.getStatusCodeValue());
	}

	@Test
	@DisplayName("IntegrationServiceException exception handler test.")
	void integrationServiceExceptionHandlerTest() {
		EBAOAdaptorException ebaoException = new EBAOAdaptorException(400, "Invalid data.");

		ExecutionException exception = new ExecutionException("Error occured.",
				new IntegrationServiceException(ebaoException));

		ResponseEntity<Object> responseEntity = globalExceptionController.handleExecutionException(null, exception);
		ResponseEntity<Object> responseEntity2 = globalExceptionController.handleExecutionException(null,
				new IntegrationAdaptorException(StringUtils.EMPTY));
		assertEquals(400, responseEntity.getStatusCodeValue());
		assertEquals(500, responseEntity2.getStatusCodeValue());
	}

	@DisplayName("SubmitApplicationException exception handler test.")
	@ParameterizedTest(name = "SubmitApplicationException exception handler with ''{0}'' test.")
	@ValueSource(ints = { 400, 404, 500 })
	void submitApplicationExceptionHandler400Test(int statusCode) {
		JSONObject responseJson = new JSONObject();
		responseJson.put("message", "Test");
		SubmitApplicationException exception = new SubmitApplicationException(
				"Error occured while submitting the policy.", statusCode, responseJson.toString());
		ResponseEntity<Object> responseEntity = globalExceptionController.handleSubmitApplicationException(null,
				exception);
		assertEquals(statusCode, responseEntity.getStatusCodeValue());
	}

}
