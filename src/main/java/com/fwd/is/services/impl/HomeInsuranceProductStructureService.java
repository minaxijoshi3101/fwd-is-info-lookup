package com.fwd.is.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.home.HomeCodeTable;
import com.fwd.data.common.home.HomeData;
import com.fwd.data.common.home.HomeOwnerInfoResponse;
import com.fwd.data.common.home.HomeType;
import com.fwd.data.common.home.PolicyHolderType;
import com.fwd.ia.common.exception.RequestFailedException;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.services.ParallelExecutorService;
import com.fwd.is.common.task.ProductStructureTask;
import com.fwd.is.services.ProductStructureService;
import com.fwd.is.util.HideHomeInsuranceQuestionTask;
import com.fwd.is.util.HomeInsuranceProductStructureUtil;

@Service("homeInsuranceProductStructureService")
public class HomeInsuranceProductStructureService implements ProductStructureService<String, HomeOwnerInfoResponse> {

	private static final Logger LOG = LogManager.getLogger(HomeInsuranceProductStructureService.class);

	private static final List<String> PRODUCT_TYPES_LIST = Arrays.asList("HOMEOWNER", "TENANT");
	private static final String POLICY_HDB_TABLE_NAME = "PolicyRiskADDRESSHDBType";
	private static final String POLICY_CONDO_TABLE_NAME = "PolicyRiskADDRESSCondoType";
	private static final String POLICY_TERM_TABLE_NAME = "PolicyLobHMTerm";

	private static final String HIDE_HOME_INSURANCE_QUESTION = "hideHomeInsuranceQuestion";

	@Value("${home.product.code}")
	private String homeProductCode;
	
	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Value("${home.landed.data}")
	private String landedJson;

	@Value("${homeowner.data}")
	private String homeOwnerJson;

	@Value("${tenant.data}")
	private String tenantJson;

	@Autowired
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private HomeInsuranceProductStructureUtil productStructureUtil;

	@Autowired
	private ParallelExecutorService parallelExecutorService;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HomeOwnerInfoResponse getStructure(String policyHolderType) {
		try {
			String reqId = String.format("REQID::%s::REQID", UUID.randomUUID().toString());
			LOG.info("{} START++++ Get Product Structure ++++START", reqId);

			if (!PRODUCT_TYPES_LIST.contains(policyHolderType)) {
				LOG.info("Invalid policy holder type. policyHolderType: {}", policyHolderType);
				throw new RequestFailedException(
						"Policy holder type is invalid, only HOMEOWNER or TENANT are allowed.");
			}

			// Get Product Id
			String productId = ebaoAdaptor.getProductId(homeProductCode);

			// ###### Parallel Execution - START #######
			Map<String, Callable<?>> tasks = new HashMap<>();
			tasks.put(POLICY_HDB_TABLE_NAME, new ProductStructureTask(POLICY_HDB_TABLE_NAME, productId, ebaoAdaptor,
					objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<HomeCodeTable>>() {
					}));
			tasks.put(POLICY_CONDO_TABLE_NAME, new ProductStructureTask(POLICY_CONDO_TABLE_NAME, productId, ebaoAdaptor,
					objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<HomeCodeTable>>() {
					}));
			tasks.put(POLICY_TERM_TABLE_NAME, new ProductStructureTask(POLICY_TERM_TABLE_NAME, productId, ebaoAdaptor,
					objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<HomeCodeTable>>() {
					}));
			//As per client suggested below api call for ajax is not required any more.
			//tasks.put(HIDE_HOME_INSURANCE_QUESTION, new HideHomeInsuranceQuestionTask(productStructureUtil));

			Map<String, ?> parallelExecutorResult = parallelExecutorService.executeHeterogenous(tasks);
			// ###### Parallel Execution - END #######

			List<HomeType> homeTypeList = new ArrayList<>();

			// landed data
			homeTypeList.add(objectMapper.readValue(landedJson, HomeType.class));

			// HDB Type List, Condo Type List and Term List
			homeTypeList.add(populateHomeTypeDate(POLICY_HDB_TABLE_NAME,
					(List<HomeCodeTable>) parallelExecutorResult.get(POLICY_HDB_TABLE_NAME)));
			homeTypeList.add(populateHomeTypeDate(POLICY_CONDO_TABLE_NAME,
					(List<HomeCodeTable>) parallelExecutorResult.get(POLICY_CONDO_TABLE_NAME)));

			// Set terms, homeTypes, and policyHolderTypes
			HomeData data = new HomeData();
			data.setTerms((List<HomeCodeTable>) parallelExecutorResult.get(POLICY_TERM_TABLE_NAME));
			data.setHomeTypes(homeTypeList);
			data.setPolicyHolderTypes(populatePolicyHolderTypes(policyHolderType));
			//As per client suggested below api call for ajax is not required any more.
			//data.setHideHomeInsuranceQuestion((boolean) ((List) parallelExecutorResult.get(HIDE_HOME_INSURANCE_QUESTION)).get(0));

			// Build API Response
			HomeOwnerInfoResponse homeOwnerInfoResponse = new HomeOwnerInfoResponse();
			homeOwnerInfoResponse.setStatusCode(successStatusCode);
			homeOwnerInfoResponse.setMessage(Constants.SUCCESS);
			homeOwnerInfoResponse.setData(data);

			LOG.info("{} END++++ Get Product Structure ++++END", reqId);
			return homeOwnerInfoResponse;
		} catch (JsonProcessingException e1) {
			String err = "Error occured while parsing json. " + e1.getMessage();
			LOG.error(err, e1);
			throw new JsonParsingException(err, e1);
		}
	}

	private List<PolicyHolderType> populatePolicyHolderTypes(String policyHolderType) throws JsonProcessingException {
		List<PolicyHolderType> policyHolderTypes = null;
		if ("HOMEOWNER".equals(policyHolderType)) {
			policyHolderTypes = objectMapper.readValue(homeOwnerJson,
					new TypeReference<List<PolicyHolderType>>() {
					});

		} else if ("TENANT".equals(policyHolderType)) {
			policyHolderTypes = objectMapper.readValue(tenantJson, new TypeReference<List<PolicyHolderType>>() {
			});
		}
		return policyHolderTypes;
	}

	private HomeType populateHomeTypeDate(String tableName, List<HomeCodeTable> codeList) {
		HomeType homeTypeData = new HomeType();
		if (POLICY_HDB_TABLE_NAME.equals(tableName)) {
			homeTypeData.setCode("HDB");
			homeTypeData.setDescription("HDB");
			homeTypeData.setId("HDB");

		} else if (POLICY_CONDO_TABLE_NAME.equals(tableName)) {
			homeTypeData.setCode("Condo");
			homeTypeData.setDescription("Condo/ Executive Condo");
			homeTypeData.setId("Condo");
		}
		homeTypeData.setRoomTypes(codeList);
		return homeTypeData;
	}

	public void setLandedJson(String landedJson) {
		this.landedJson = landedJson;
	}

	public void setSuccessStatusCode(String successStatusCode) {
		this.successStatusCode = successStatusCode;
	}
}