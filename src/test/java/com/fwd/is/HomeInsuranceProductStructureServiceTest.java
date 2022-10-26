package com.fwd.is;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.home.HomeOwnerInfoResponse;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.ia.ebao.adaptor.exceptions.EBAOAdaptorException;
import com.fwd.is.common.exceptions.IntegrationServiceException;
import com.fwd.is.services.impl.HomeInsuranceProductStructureService;
import com.fwd.is.util.HomeInsuranceProductStructureUtil;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
class HomeInsuranceProductStructureServiceTest {

	private static final String INVALID_PRODUCT_CODE_JSON = "{ \"message\":\"Invalid product code.\" }";
	private static final String RESOURCE_NOT_FOUND = "Resource Not Found.";
	private static final String INVALID_PRODUCT_CODE = "TX";
	private static final String INVALID_LANDED_JSON = "{ \"test\":\"dummy\" }";
	private static final String POLICY_HOLDER_TYPE_HOMEOWNER = "HOMEOWNER";
	private static final String POLICY_HOLDER_TYPE_TENANT = "TENANT";
	private static final String POLICY_HOLDER_TYPE_INVALID = "TestType";

	@Value("${api.home.insurance.mapping}")
	private String baseUrl;

	@Value("${hide.home.insurance.question.url}")
	private String hideHomeInsuranceQuestionURL;

	@Value("${home.landed.data}")
	private String landedJson;

	@Autowired
	private MockMvc mockMvc;

	@InjectMocks
	private HomeInsuranceProductStructureService mockHomeInsuranceProductStructureService;

	@Mock
	private EBAOAdaptor mockEbaoAdaptor;

	@InjectMocks
	private HomeInsuranceProductStructureUtil homeInsuranceProductStructureUtil;

	@Mock
	private RestTemplate restTemplate;

	@Test
	@DisplayName("Get product structure positive response with policyHolderType as HOMEOWNER.")
	void getProductStructureForHomeownerPositiveTest() throws Exception {
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_HOMEOWNER))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals("0", response.getString("statusCode"));
		assertEquals("success", response.getString("message"));
		assertNotNull(response.getJSONObject("data"));
	}

	@Test
	@DisplayName("Get product structure negative response with invalid method type.")
	void getProductStructureNegativeWithInvalidMethodTypeTest() throws Exception {
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.post(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_HOMEOWNER))
				.andExpect(status().is4xxClientError()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals("Method not allowed.", response.getString("message"));
	}

	@Test
	@DisplayName("Get product structure positive response with policyHolderType as TENANT.")
	void getProductStructureForTenantPositiveTest() throws Exception {
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_TENANT))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals("0", response.getString("statusCode"));
		assertEquals("0", response.getString("statusCode"));
		assertEquals("success", response.getString("message"));
		assertNotNull(response.getJSONObject("data"));
	}

	@Test
	@DisplayName("Get product structure negative response with invalid policyHolderType.")
	void getProductStructureNegativeWithInvalidPolicyHolderTypeTest() throws Exception {
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_INVALID))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		HomeOwnerInfoResponse response = new ObjectMapper().readValue(result, HomeOwnerInfoResponse.class);
		assertNotNull(response);
		assertEquals("Bad request", response.getMessage());
	}

	@Test
	@DisplayName("Get product structure throws JsonProcessingException.")
	void getProductStructureThrowsJsonProcessingExceptionTest() {
		mockHomeInsuranceProductStructureService.setLandedJson(INVALID_LANDED_JSON);
		assertThrows(IntegrationServiceException.class,
				() -> mockHomeInsuranceProductStructureService.getStructure(POLICY_HOLDER_TYPE_HOMEOWNER));
	}

	@Test
	@DisplayName("Get product structure throws EBAOAdaptorException when resource not found.")
	void getProductStructureThrowsEBAOAdaptorExceptionTest() {
		mockHomeInsuranceProductStructureService.setLandedJson(landedJson);
		mockHomeInsuranceProductStructureService.setHomeProductCode(INVALID_PRODUCT_CODE);
		when(mockEbaoAdaptor.getProductId(INVALID_PRODUCT_CODE))
				.thenThrow(new EBAOAdaptorException(RESOURCE_NOT_FOUND, 404, null));
		HomeOwnerInfoResponse response = mockHomeInsuranceProductStructureService
				.getStructure(POLICY_HOLDER_TYPE_HOMEOWNER);
		assertEquals(RESOURCE_NOT_FOUND, response.getMessage());
	}

	@Test
	@DisplayName("Get product structure throws EBAOAdaptorException when product code is invalid.")
	void getProductStructureThrowsEBAOAdaptorExceptionWithInvalidProductCodeTest() {
		mockHomeInsuranceProductStructureService.setLandedJson(landedJson);
		mockHomeInsuranceProductStructureService.setHomeProductCode(INVALID_PRODUCT_CODE);
		when(mockEbaoAdaptor.getProductId(INVALID_PRODUCT_CODE))
				.thenThrow(new EBAOAdaptorException("Bad Request", 400, INVALID_PRODUCT_CODE_JSON));
		HomeOwnerInfoResponse response = mockHomeInsuranceProductStructureService
				.getStructure(POLICY_HOLDER_TYPE_HOMEOWNER);
		assertNotNull(response.getMessages());
	}

	@Test
	@DisplayName("Get product structure throws RestClientException.")
	void getProductStructureThrowsRestClientExceptionTest() {
		mockHomeInsuranceProductStructureService.setLandedJson(landedJson);
		mockHomeInsuranceProductStructureService.setHomeProductCode(INVALID_PRODUCT_CODE);
		when(mockEbaoAdaptor.getProductId(INVALID_PRODUCT_CODE))
				.thenThrow(new RestClientException("Something went wrong."));
		assertThrows(IntegrationServiceException.class,
				() -> mockHomeInsuranceProductStructureService.getStructure(POLICY_HOLDER_TYPE_HOMEOWNER));
	}

	@Test
	@DisplayName("Get hide home insurance question with null response.")
	void getHideHomeInsuranceQuestionWithNullResponseTest() {
		String responseBody = null;
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class)).thenReturn(responseEntity);
		Object response = homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion();
		assertNull(response);
	}

	@Test
	@DisplayName("Get hide home insurance question throws IntegrationServiceException.")
	void getHideHomeInsuranceQuestionThrowsIntegrationServiceExceptionTest() {
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class)).thenReturn(responseEntity);
		assertThrows(IntegrationServiceException.class,
				() -> homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion());
	}

	@Test
	@DisplayName("Get hide home insurance question throws RestClientException.")
	void getHideHomeInsuranceQuestionThrowsRestClientExceptionTest() {
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class))
				.thenThrow(new RestClientException("Something went wrong."));
		assertThrows(IntegrationServiceException.class,
				() -> homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion());
	}

	@Test
	@DisplayName("Get hide home insurance question throws JSONException.")
	void getHideHomeInsuranceQuestionThrowsJSONExceptionTest() {
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class))
				.thenThrow(new JSONException("Exception occured while parsing json."));
		assertThrows(IntegrationServiceException.class,
				() -> homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion());
	}

	@Test
	void testMain() {
		InfoLookupStarter.main(new String[] {});
		assertTrue(true);
	}

}
