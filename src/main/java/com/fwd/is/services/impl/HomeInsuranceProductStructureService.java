package com.fwd.is.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.home.CodeTable;
import com.fwd.data.common.home.Data;
import com.fwd.data.common.home.HomeOwnerInfoResponse;
import com.fwd.data.common.home.HomeType;
import com.fwd.data.common.home.PolicyHolderType;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.ia.ebao.adaptor.exceptions.EBAOAdaptorException;
import com.fwd.is.common.exceptions.IntegrationServiceException;
import com.fwd.is.services.ProductStructureService;
import com.fwd.is.util.HomeInsuranceProductStructureUtil;

@Service("homeInsuranceProductStructureService")
public class HomeInsuranceProductStructureService implements ProductStructureService<String, HomeOwnerInfoResponse> {

	private static final Logger LOG = LogManager.getLogger(HomeInsuranceProductStructureService.class);

	private static final List<String> PRODUCT_TYPES = Arrays.asList("HOMEOWNER", "TENANT");

	@Value("${home.product.code}")
	private String homeProductCode;

	@Value("#{${home.product.table.names}}")
	private Set<String> tableNames;

	@Value("${home.landed.data}")
	private String landedJson;

	@Value("${homeowner.data}")
	private String homeOwnerJson;

	@Value("${tenant.data}")
	private String tenantJson;

	@Resource(name = "ebaoAdaptor")
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private HomeInsuranceProductStructureUtil productStructureUtil;

	@Override
	public HomeOwnerInfoResponse getStructure(String policyHolderType) {
		HomeOwnerInfoResponse homeOwnerInfoResponse = new HomeOwnerInfoResponse();
		if (!PRODUCT_TYPES.contains(policyHolderType)) {
			homeOwnerInfoResponse.setMessage("Bad request");
			return homeOwnerInfoResponse;
		}
		try {
			homeOwnerInfoResponse.setStatusCode("0");
			homeOwnerInfoResponse.setMessage("success");
			Data data = new Data();
			List<HomeType> homeTypeList = new ArrayList<>();
			// landed data
			HomeType landedData = new ObjectMapper().readValue(landedJson, HomeType.class);
			homeTypeList.add(landedData);

			// Get Product Id
			String productId = ebaoAdaptor.getProductId(homeProductCode);

			// Get Code List
			for (String tableName : tableNames) {
				List<CodeTable> codeList = ebaoAdaptor.getCodeTableList(tableName, productId);
				if ("PolicyLobHMTerm".equals(tableName)) {
					data.setTerms(codeList);
				} else {
					homeTypeList.add(populateHomeTypeDate(tableName, codeList));
				}
			}
			// Home Types
			data.setHomeTypes(homeTypeList);
			// Policy Holder Types
			data.setPolicyHolderTypes(populatePolicyHolderTypes(policyHolderType));
			data.setHideHomeInsuranceQuestion(productStructureUtil.getHideHomeInsuranceQuestion());
			homeOwnerInfoResponse.setData(data);

			return homeOwnerInfoResponse;

		} catch (JsonProcessingException e) {
			LOG.error("Exception occurred while parsing json.", e);
			throw new IntegrationServiceException("Exception occurred while parsing json.", e);

		} catch (EBAOAdaptorException e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			}

			HomeOwnerInfoResponse response = new HomeOwnerInfoResponse();
			if ((Integer) e.getStatusCode() == 404) {
				response.setMessage("Resource Not Found.");
				return response;
			} else {
				response.setMessages(new JSONObject(e.getResponseJson()).toMap());
				return response;
			}
		} catch (Exception e) {
			if (LOG.isDebugEnabled()) {
				LOG.debug(e.getMessage(), e);
			}
			throw new IntegrationServiceException("Exception occured while retrieving home owner info.", e);
		}
	}

	private List<PolicyHolderType> populatePolicyHolderTypes(String policyHolderType) throws JsonProcessingException {
		List<PolicyHolderType> policyHolderTypes = null;
		if ("HOMEOWNER".equals(policyHolderType)) {
			PolicyHolderType[] poliHolderTypesArray = new ObjectMapper().readValue(homeOwnerJson,
					PolicyHolderType[].class);
			policyHolderTypes = Arrays.asList(poliHolderTypesArray);

		} else if ("TENANT".equals(policyHolderType)) {
			PolicyHolderType[] poliHolderTypesArray = new ObjectMapper().readValue(tenantJson,
					PolicyHolderType[].class);
			policyHolderTypes = Arrays.asList(poliHolderTypesArray);
		}
		return policyHolderTypes;
	}

	private HomeType populateHomeTypeDate(String tableName, List<CodeTable> codeList) {
		HomeType homeTypeData = new HomeType();
		if ("PolicyRiskADDRESSHDBType".equals(tableName)) {
			homeTypeData.setCode("HDB");
			homeTypeData.setDescription("HDB");
			homeTypeData.setId("HDB");

		} else if ("PolicyRiskADDRESSCondoType".equals(tableName)) {
			homeTypeData.setCode("Condo");
			homeTypeData.setDescription("Condo/ Executive Condo");
			homeTypeData.setId("Condo");
		}
		homeTypeData.setRoomTypes(codeList);
		return homeTypeData;
	}

	public void setHomeProductCode(String homeProductCode) {
		this.homeProductCode = homeProductCode;
	}

	public void setLandedJson(String landedJson) {
		this.landedJson = landedJson;
	}
}