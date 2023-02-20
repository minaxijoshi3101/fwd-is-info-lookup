package com.fwd.is;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.commcar.CommCarModelListResponse;
import com.fwd.data.common.commcar.CommCarProductStructureResponse;
import com.fwd.data.common.commcar.FinanceCompanyResponse;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.controllers.CommCarInsuranceInfoLookupController;
import com.fwd.is.facade.CommCarInsuranceInfoLookupFacade;
import com.fwd.is.facade.impl.CommCarInsuranceInfoLookupFacadeImpl;
import com.fwd.is.services.LookupService;
import com.fwd.is.services.ModelByMakeService;
import com.fwd.is.services.ProductStructureService;
import com.fwd.is.services.impl.CommCarInsuranceFinanceCompanyService;
import com.fwd.is.services.impl.CommCarInsuranceModelByMakeService;

@SpringBootTest(classes = InfoLookupStarter.class)
@AutoConfigureMockMvc
class CommCarInsuranceApiTest {

	@InjectMocks
	private CommCarInsuranceInfoLookupController commCarInsuranceInfoLookupController;

	@InjectMocks
	private CommCarInsuranceInfoLookupFacadeImpl commCarInsuranceInfoLookupFacadeImpl;

	@InjectMocks
	private CommCarInsuranceModelByMakeService commCarInsuranceModelByMakeService;

	@InjectMocks
	private CommCarInsuranceFinanceCompanyService commCarInsuranceFinanceCompanyService;
	
	@Mock
	private CommCarInsuranceInfoLookupFacade commCarInsuranceInfoLookupFacade;

	@Mock
	private ProductStructureService<String, CommCarProductStructureResponse> mockCommCarInsuranceProductStructureService;

	@Mock
	private ModelByMakeService<String, CommCarModelListResponse> mockCommCarInsuranceModelByMakeService;

	@Mock
	private LookupService<String, FinanceCompanyResponse> mockCommCarInsuranceFinanceCompanyService;

	@Mock
	private EBaoUtil mocEbaoUtil;

	@Mock
	private ObjectMapper mockObjectMapper;
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Mock
	private EBAOAdaptor ebaoAdaptor;
	
	@Value("${commcar.product.code}")
	private String productCode;
	
	@Value("${api.commcar.insurance.mapping}")
	private String baseUrl;

	@Test
	@DisplayName("Get info controller test.")
	void getInfoControllerTest() throws Exception {

		CommCarProductStructureResponse commCarProductStructureResponse = new CommCarProductStructureResponse();
		when(commCarInsuranceInfoLookupFacade.getProductStructure()).thenReturn(commCarProductStructureResponse);

		ResponseEntity<CommCarProductStructureResponse> response = commCarInsuranceInfoLookupController.getCarInfo();
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	@DisplayName("Get Model List controller test.")
	void getModelListControllerTest() throws Exception {

		CommCarModelListResponse commCarModelListResponse = new CommCarModelListResponse();
		when(commCarInsuranceInfoLookupFacade.getModelList(anyString())).thenReturn(commCarModelListResponse);

		ResponseEntity<CommCarModelListResponse> response = commCarInsuranceInfoLookupController.getModelList("BMW");
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	@DisplayName("Get Finance Company controller test.")
	void getFinanceCompanyControllerTest() throws Exception {

		FinanceCompanyResponse financeCompanyResponse = new FinanceCompanyResponse();
		when(commCarInsuranceInfoLookupFacade.getFinanceCompany()).thenReturn(financeCompanyResponse);

		ResponseEntity<FinanceCompanyResponse> response = commCarInsuranceInfoLookupController.getFinanceCompany();
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	@DisplayName("Get info facade test.")
	void getInfoFacadeTest() throws Exception {

		CommCarProductStructureResponse commCarProductStructureResponse = new CommCarProductStructureResponse();
		when(mockCommCarInsuranceProductStructureService.getStructure(anyString()))
				.thenReturn(commCarProductStructureResponse);

		CommCarProductStructureResponse response = commCarInsuranceInfoLookupFacadeImpl.getProductStructure();
		assertThat(response).isNotNull().isEqualTo(commCarProductStructureResponse);
	}

	@Test
	@DisplayName("Get Model List facade test.")
	void getModelListFacadeTest() throws Exception {

		CommCarModelListResponse commCarModelListResponse = new CommCarModelListResponse();
		when(mockCommCarInsuranceModelByMakeService.getModelList(anyString())).thenReturn(commCarModelListResponse);

		CommCarModelListResponse response = commCarInsuranceInfoLookupFacadeImpl.getModelList("BMW");
		assertThat(response).isNotNull().isEqualTo(commCarModelListResponse);
	}

	@Test
	@DisplayName("Get Finance Company facade test.")
	void getFinanceCompanyFacadeTest() throws Exception {

		FinanceCompanyResponse financeCompanyResponse = new FinanceCompanyResponse();
		when(mockCommCarInsuranceFinanceCompanyService.getFinanceCompany(anyString()))
				.thenReturn(financeCompanyResponse);

		FinanceCompanyResponse response = commCarInsuranceInfoLookupFacadeImpl.getFinanceCompany();
		assertThat(response).isNotNull().isEqualTo(financeCompanyResponse);
	}
	
	@Test
	@DisplayName("Get info test.")
	void getInfoTest() throws Exception {
		String response = getProductStructureTestEndpoint();
		CommCarProductStructureResponse carInfoResponse = (CommCarProductStructureResponse) objectMapper.readValue(response,
				CommCarProductStructureResponse.class);
		assertNotNull(carInfoResponse);
	}
	
	private String getProductStructureTestEndpoint() throws Exception {
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/commcar/info"))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return mvcResult.getResponse().getContentAsString();
	}

	@Test
	@DisplayName("get Model List Validation Test")
	void getStructureForCommcarValidationTest() throws Exception {

		CommCarModelListResponse response = commCarInsuranceModelByMakeService.getModelList(StringUtils.EMPTY);

		assertThat(response).isNotNull();
		assertThat(response.getStatus()).isEqualTo("failed");
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("get Model List JsonProcessingException Test")
	void getStructureForCommcarJsonProcessingExceptionTest() throws Exception {

		String codeListObject = "{\"test\":\"123\"}";
		when(mocEbaoUtil.commonCodeTableByTableName(anyString(), anyString())).thenReturn(codeListObject);

		when(mockObjectMapper.readValue(anyString(), any(TypeReference.class)))
		.thenThrow(mock(JsonProcessingException.class));
		
		assertThrows(JsonParsingException.class,
				() -> commCarInsuranceModelByMakeService.getModelList("CHERY"));
	}

	@Test
	@DisplayName("get Model List success Test")
	void getStructureForCommcarSuccessTest() throws Exception {

		String codeListObject = "{\"models\":[{\"Code\":\"A1 1.3\",\"Description\":\"A1 1.3\",\"Id\":\"A1 1.3\",\"ConditionFields\":[{\"AUTO_LINE\":\"PV\",\"MAKE\":\"CHERY\"},{\"AUTO_LINE\":\"CV\",\"MAKE\":\"CHERY\"}]}]}";
		when(mocEbaoUtil.commonCodeTableByTableName(anyString(), anyString())).thenReturn(codeListObject);

		CommCarModelListResponse response = commCarInsuranceModelByMakeService.getModelList("CHERY");

		assertThat(response).isNotNull();
	}
	
	@Test
	@DisplayName("Get finance Company test.")
	void getFinanceCompanyTest() throws Exception {
		String response = getFinanceCompanyTestEndpoint();
		FinanceCompanyResponse carInfoResponse = (FinanceCompanyResponse) objectMapper.readValue(response,
				FinanceCompanyResponse.class);
		assertNotNull(carInfoResponse);
	}
	
	private String getFinanceCompanyTestEndpoint() throws Exception {
		MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get(baseUrl + "/commcar/info/financeCompany"))
				.andExpect(status().is2xxSuccessful()).andReturn();
		return mvcResult.getResponse().getContentAsString();
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("get finance Company JsonProcessingException Test")
	void getFinanceCompanyJsonProcessingExceptionTest() throws Exception {
		commCarInsuranceFinanceCompanyService.setProductCode(productCode);
		
		String productId = "1243543";
		when(ebaoAdaptor.getProductIdVer2(productCode)).thenReturn(productId);
		
		String codeListObject = "payload";
		when(ebaoAdaptor.codeTableByTableName(anyString(), anyString(), anyString())).thenReturn(codeListObject);

		when(mockObjectMapper.readValue(anyString(), any(TypeReference.class)))
		.thenThrow(mock(JsonProcessingException.class));
		
		assertThrows(JsonParsingException.class,
				() -> commCarInsuranceFinanceCompanyService.getFinanceCompany("CHERY"));
	}
}
