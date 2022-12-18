package com.fwd.is;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import com.fwd.data.common.car.CarProductStructureResponse;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.impl.CarInsuranceProductStructureService;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
class CarInsuranceGetProductStructureApiTest {

	@Value("${api.car.insurance.mapping}")
	private String baseUrl;
	
	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Value("#{${car.product.structure.table.names}}")
	private Map<String, String> tableNames;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@InjectMocks
	private CarInsuranceProductStructureService carInsuranceProductStructureService;

	@Mock
	private EBaoUtil mocEbaoUtil;

	@Mock
	private ObjectMapper mockObjectMapper;

	@Test
	@DisplayName("Get product structure positive test.")
	void getProductStructurePositiveTest() throws Exception {
		carInsuranceProductStructureService.setSuccessStatusCode(successStatusCode);
		String response = getProductStructureTestEndpoint();
		CarProductStructureResponse carInfoResponse = (CarProductStructureResponse) objectMapper.readValue(response,
				CarProductStructureResponse.class);
		assertNotNull(carInfoResponse);
		assertEquals("success", carInfoResponse.getStatus());
		assertEquals(0, carInfoResponse.getStatusCode());
		assertEquals("Successfully return product structure", carInfoResponse.getMessage());
	}

	private String getProductStructureTestEndpoint() throws Exception {
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/car/getProductStructure"))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return mvcResult.getResponse().getContentAsString();
	}

}
