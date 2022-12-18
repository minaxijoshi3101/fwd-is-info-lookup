package com.fwd.is;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.impl.CarInsuranceModelByMakeService;
import com.fwd.is.util.HideHomeInsuranceQuestionTask;
import com.fwd.is.util.ParseFileContentTask;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class CarInsuranceGetModelListApiTest {

	private static final String CAR_CODE_TYPE_INVALID = "TestType";
	private static final String CAR_CODE_TYPE_VALID = "BMW";

	@Value("${api.car.insurance.mapping}")
	private String baseUrl;

	@Value("${api.success.status.code}")
	private String successStatusCode;

	@InjectMocks
	private CarInsuranceModelByMakeService carInsuranceCarStructureService;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private EBaoUtil mockEbaoUtil;

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("get Model List Success Test")
	void getStructureForCarPositiveTest() throws Exception {
		carInsuranceCarStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/car/" + CAR_CODE_TYPE_VALID + "/getModelList"))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals(0, response.get("status_code"));
		assertThat(response.getString("status")).isEqualTo("success");
		assertNotNull(response.get("data"));
	}

	@Test
	@DisplayName("get Model List Negative Test")
	void getStructureForCarNegativeTest() throws Exception {
		carInsuranceCarStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/car/" + CAR_CODE_TYPE_INVALID + "/getModelList"))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals(0, response.get("status_code"));
		assertThat(response.getString("status")).isEqualTo("success");
	}

	@Test
	@DisplayName("Without required field Test")
	void getWithoutRequiredFieldTest() throws Exception {
		carInsuranceCarStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/car/" + "     " + "/getModelList"))
				.andExpect(status().is4xxClientError()).andReturn();
		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertThat(response.getString("status")).isEqualTo("failed");
		assertThat(response.getString("message")).isEqualTo("Car Make is Empty");

	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("Get model list throws JsonProcessingException test.")
	void getModelListThrowsJsonProcessingExceptionTest() throws JsonMappingException, JsonProcessingException {
		carInsuranceCarStructureService.setSuccessStatusCode(successStatusCode);
		String invalidJson = "{\"test\":\"123\"}";
		when(mockEbaoUtil.commonCodeTableByTableName(anyString(), anyString())).thenReturn(invalidJson);
		when(objectMapper.readValue(anyString(), any(TypeReference.class)))
				.thenThrow(mock(JsonProcessingException.class));
		assertThrows(JsonParsingException.class,
				() -> carInsuranceCarStructureService.getModelList(CAR_CODE_TYPE_VALID));
	}

	@Test
	@DisplayName("Parallel task classes test.")
	void parallelTaskClassesTest() {
		HideHomeInsuranceQuestionTask task1 = new HideHomeInsuranceQuestionTask();
		ParseFileContentTask task2 = new ParseFileContentTask();
		assertNotNull(task1);
		assertNotNull(task2);
	}
}
