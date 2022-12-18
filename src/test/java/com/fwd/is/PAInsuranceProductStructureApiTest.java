package com.fwd.is;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.services.impl.PAInsuranceProductStructureService;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
@TestInstance(Lifecycle.PER_CLASS)
class PAInsuranceProductStructureApiTest {

	@Value("${api.pa.insurance.mapping}")
	private String baseUrl;
	
	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Autowired
	private MockMvc mockMvc;

	@InjectMocks
	private PAInsuranceProductStructureService paInsuranceProductStructureService;

	@Mock
	private EBAOAdaptor ebaoAdaptor;

	@Mock
	private ObjectMapper mockObjectMapper;

	@Test
	@DisplayName("Get product structure positive test.")
	void getProductStructurePositiveTest() throws Exception {
		paInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/pa/v1/getProductStructure"))
				.andExpect(status().is2xxSuccessful()).andReturn();
		String content = mvcResult.getResponse().getContentAsString();
		JSONObject response = new JSONObject(content);
		assertNotNull(response);
		assertEquals("success", response.getString("status"));
		assertEquals("0", response.getString("status_code"));
		assertEquals("Successfully retrieved product structure", response.getString("message"));
	}

}
