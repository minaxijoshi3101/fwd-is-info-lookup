package com.fwd.is;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.annotation.PostConstruct;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.is.common.exceptions.IntegrationServiceException;
import com.fwd.is.common.exceptions.RedisException;
import com.fwd.is.common.services.RedisCacheService;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.impl.HomeInsuranceProductStructureService;
import com.fwd.is.util.HomeInsuranceProductStructureUtil;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
class HomeInsuranceGetProductStructureApiTest {

	private static final String POLICY_HOLDER_TYPE_HOMEOWNER = "HOMEOWNER";
	private static final String POLICY_HOLDER_TYPE_TENANT = "TENANT";
	private static final String POLICY_HOLDER_TYPE_INVALID = "TestType";

	@Value("${api.home.insurance.mapping}")
	private String baseUrl;

	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Value("${hide.home.insurance.question.url}")
	private String hideHomeInsuranceQuestionURL;

	@Value("${home.landed.data}")
	private String landedJson;

	@Autowired
	private MockMvc mockMvc;

	@InjectMocks
	private HomeInsuranceProductStructureService mockHomeInsuranceProductStructureService;

	@Mock
	private ObjectMapper mockObjectMapper;

	@Mock
	private EBaoUtil mockEbaoUtil;

	@InjectMocks
	private HomeInsuranceProductStructureUtil homeInsuranceProductStructureUtil;

	@Mock
	private RestTemplate restTemplate;

	private static RedisCacheService redisCacheService;

	private static ApplicationContext context;

	@Autowired
	public HomeInsuranceGetProductStructureApiTest(ApplicationContext applicationContext) {
		context = applicationContext;
	}

	@PostConstruct
	static void initializeBeansTestSetUp() {
		redisCacheService = context.getBean(RedisCacheService.class);
	}

	@AfterAll
	static void flushAllRedisCaches() throws RedisException {
		redisCacheService.evictAllCaches();
	}

	@Test
	@DisplayName("Get product structure positive response with policyHolderType as HOMEOWNER.")
	void getProductStructureForHomeownerPositiveTest() throws Exception {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
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
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
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
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_TENANT))
				.andExpect(status().is2xxSuccessful()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals("0", response.getString("statusCode"));
		assertEquals("success", response.getString("message"));
		assertNotNull(response.getJSONObject("data"));
	}

	@Test
	@DisplayName("Get product structure negative response with invalid policyHolderType.")
	void getProductStructureNegativeWithInvalidPolicyHolderTypeTest() throws Exception {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc
				.perform(MockMvcRequestBuilders.get(baseUrl + "/home/info/" + POLICY_HOLDER_TYPE_INVALID))
				.andExpect(status().is4xxClientError()).andReturn();

		String result = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(result);
		assertNotNull(response);
		assertEquals("5", response.getString("status_code"));
	}

	@Test
	@DisplayName("Get hide home insurance question with null response.")
	void getHideHomeInsuranceQuestionWithNullResponseTest() {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		String responseBody = null;
		ResponseEntity<String> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class)).thenReturn(responseEntity);
		boolean response = homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion();
		assertFalse(response);
	}

	@Test
	@DisplayName("Get hide home insurance question throws IntegrationServiceException.")
	void getHideHomeInsuranceQuestionThrowsIntegrationServiceExceptionTest() {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class)).thenReturn(responseEntity);
		assertThrows(IntegrationServiceException.class,
				() -> homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion());
	}

	@Test
	@DisplayName("Get hide home insurance question throws RestClientException.")
	void getHideHomeInsuranceQuestionThrowsRestClientExceptionTest() {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		homeInsuranceProductStructureUtil.setHideHomeInsuranceQuestionURL(hideHomeInsuranceQuestionURL);
		when(restTemplate.getForEntity(hideHomeInsuranceQuestionURL, String.class))
				.thenThrow(new RestClientException("Something went wrong."));
		assertThrows(IntegrationServiceException.class,
				() -> homeInsuranceProductStructureUtil.getHideHomeInsuranceQuestion());
	}

	@Test
	@DisplayName("Get hide home insurance question throws JSONException.")
	void getHideHomeInsuranceQuestionThrowsJSONExceptionTest() {
		mockHomeInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
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
