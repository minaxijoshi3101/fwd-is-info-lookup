package com.fwd.is.services.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.CodeTable;
import com.fwd.data.common.car.CountriesList;
import com.fwd.data.common.pa.InsuredOption;
import com.fwd.data.common.pa.Occupation;
import com.fwd.data.common.pa.PAStructureData;
import com.fwd.data.common.pa.PAStructureJsonData;
import com.fwd.data.common.pa.ProductStructureData;
import com.fwd.data.common.pa.ProductStructureResponse;
import com.fwd.data.common.pamaid.FrontendData;
import com.fwd.data.common.pamaid.PolicyDetails;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;
import com.fwd.is.common.ifwd_data.domain.Config;
import com.fwd.is.common.ifwd_data.repository.ConfigRepository;
import com.fwd.is.common.services.ParallelExecutorService;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.PAStructureService;
import com.fwd.is.tasks.DiscountRateFromCodeTask;
import com.fwd.is.tasks.ProductStructureTask;
import com.fwd.is.util.ParseFileContentTask;

@Service("paInsuranceProductStructureService")
public class PAInsuranceProductStructureService implements PAStructureService<String, ProductStructureResponse> {

	private static final Logger LOG = LogManager.getLogger(PAInsuranceProductStructureService.class);

	private static final String PRODUCT_PA = "PA";
	private static final String ID = "Id";
	private static final String LABEL = "Label";
	private static final String TOOLTIP_LABEL = "TooltipLabel";
	private static final String ADDITIONAL_QUESTION_AMOUNT = "additionalQuestionAmount";
	private static final String INSURED_OPTION_LIST = "insuredOptionList";
	private static final String INSURED_TYPE_LIST = "insuredTypeList";
	private static final String OCCUPATION_LIST = "occupationList";
	private static final String POLICY_DETAILS = "policyDetails";
	private static final String CATEGORY_D2C = "D2C";
	private static final String KEY_PA_LABELS_DATA = "PA_LABELS_DATA";
	private static final String COUNTRIES = "countries";

	@Value("${pa.product.code}")
	private String paProductCode;

	@Value("${contries.list.file.path}")
	private String countriesFilePath;

	@Value("${policy.details.file.path}")
	private String policyDetailsFilePath;

	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Resource(name = "ebaoAdaptor")
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private EBaoUtil eBaoUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@PersistenceContext(name = "ifwdDataEntityManager")
	private EntityManager ifwdDataEntityManager;

	@Autowired
	private ConfigRepository configRepository;

	@Autowired
	private ParallelExecutorService parallelExecutorService;

	@Override
	public ProductStructureResponse getStructure(String params) {
		String reqId = String.format("REQID::%s::REQID", UUID.randomUUID().toString());
		LOG.info("{} START++++ Get Product Structure ++++START", reqId);

		// Get Product Id
		String productId = ebaoAdaptor.getProductId(paProductCode);

		// ##### Parallel Execution - START ######
		Map<String, Callable<?>> tasks = new HashMap<>();
		tasks.put(OCCUPATION_LIST, new ProductStructureTask(Constants.PA_OCCUPATIONCLASS_NAME, productId, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<Occupation>>() {
				}));
		tasks.put(INSURED_TYPE_LIST, new ProductStructureTask(Constants.PA_INSUREDTYPE_NAME, productId, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
				}));
		tasks.put(INSURED_OPTION_LIST, new ProductStructureTask(Constants.PA_INSUREDOPT_NAME, productId, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<InsuredOption>>() {
				}));
		tasks.put(ADDITIONAL_QUESTION_AMOUNT,
				new DiscountRateFromCodeTask(Constants.ELE_DISC_ADDITIONAL_QN, paProductCode, eBaoUtil));

		tasks.put(COUNTRIES, new ParseFileContentTask(countriesFilePath, new TypeReference<List<CountriesList>>() {
		}, objectMapper));

		tasks.put(POLICY_DETAILS, new ParseFileContentTask(policyDetailsFilePath, new TypeReference<PolicyDetails>() {
		}, objectMapper));

		Map<String, ?> parallelExecutionResult = parallelExecutorService.executeHeterogenous(tasks);
		// ##### Parallel Execution - END ######

		ProductStructureResponse response = buildAPIResponse(parallelExecutionResult);

		// Get Config by productType, Key and Category.
		Config config = configRepository.findFirstByProductTypeAndKeyAndCategoryOrderByUpdatedDateDesc(paProductCode,
				KEY_PA_LABELS_DATA, CATEGORY_D2C);
		JSONObject configValue = new JSONObject(config.getValue());

		// Map Label to insuredOptionList
		List<Occupation> tempOccupationList = mapToolTipLabelToOccupations(response,
				configValue.getJSONArray(OCCUPATION_LIST));

		// set new occupation list.
		response.getData().getJsonData().getProductStructure().setOccupationList(tempOccupationList);

		// Map tooltipLabel to occupationList
		List<InsuredOption> tempInsuredOptionList = mapLabelToInsuredOptions(response,
				configValue.getJSONArray(INSURED_OPTION_LIST));

		// set new insured option list.
		response.getData().getJsonData().getProductStructure().setInsuredOptionList(tempInsuredOptionList);

		LOG.info("{} END++++ Get Product Structure ++++END", reqId);
		return response;
	}

	private List<InsuredOption> mapLabelToInsuredOptions(ProductStructureResponse response,
			JSONArray insuredOptionArray) {
		List<InsuredOption> tempInsuredOptionList = new ArrayList<>();
		for (InsuredOption ct1 : response.getData().getJsonData().getProductStructure().getInsuredOptionList()) {
			for (Object ob : insuredOptionArray) {
				JSONObject jo = (JSONObject) ob;
				if (jo.getString(ID).equals(ct1.getId())) {
					ct1.setLabel(jo.getString(LABEL));
					tempInsuredOptionList.add(ct1);
				}
			}
		}
		return tempInsuredOptionList;
	}

	private List<Occupation> mapToolTipLabelToOccupations(ProductStructureResponse response,
			JSONArray occupationArray) {
		List<Occupation> tempOccupationList = new ArrayList<>();
		for (Occupation occupation : response.getData().getJsonData().getProductStructure().getOccupationList()) {
			for (Object ob : occupationArray) {
				JSONObject jo = (JSONObject) ob;
				if (jo.getString(ID).equals(occupation.getId())) {
					occupation.setTooltipLabel(jo.getString(TOOLTIP_LABEL));
					tempOccupationList.add(occupation);
				}
			}
		}
		return tempOccupationList;
	}

	@SuppressWarnings("unchecked")
	private ProductStructureResponse buildAPIResponse(Map<String, ?> parallelExecutionResult) {
		// Build API Response
		ProductStructureResponse response = new ProductStructureResponse();
		response.setStatus(Constants.SUCCESS);
		response.setStatusCode(successStatusCode);
		response.setMessage("Successfully retrieved product structure");

		// Data
		PAStructureData data = new PAStructureData();
		data.setFullName(StringUtils.EMPTY);
		data.setEmail(StringUtils.EMPTY);
		data.setMobile(StringUtils.EMPTY);
		data.setIdNumber(StringUtils.EMPTY);
		data.setProductType(PRODUCT_PA);
		data.setPreferredName(StringUtils.EMPTY);

		// JsonData
		PAStructureJsonData jsonData = new PAStructureJsonData();
		jsonData.setPolicyDetails((PolicyDetails) parallelExecutionResult.get(POLICY_DETAILS));
		FrontendData frontendData = new FrontendData();
		frontendData.setExcludedCoverages(Collections.emptyList());
		jsonData.setFrontendData(frontendData);

		// ProductStructureData
		ProductStructureData productStructure = new ProductStructureData();
		productStructure.setOccupationList((List<Occupation>) parallelExecutionResult.get(OCCUPATION_LIST));
		productStructure.setInsuredTypeList((List<CodeTable<Integer>>) parallelExecutionResult.get(INSURED_TYPE_LIST));
		productStructure.setInsuredOptionList((List<InsuredOption>) parallelExecutionResult.get(INSURED_OPTION_LIST));
		productStructure.setCountriesList(parallelExecutionResult.get(COUNTRIES));
		productStructure.setAdditionalQnAmount((Double) parallelExecutionResult.get(ADDITIONAL_QUESTION_AMOUNT));

		jsonData.setProductStructure(productStructure);
		data.setJsonData(jsonData);
		response.setData(data);
		return response;
	}

	public void setPaProductCode(String paProductCode) {
		this.paProductCode = paProductCode;
	}

	public void setSuccessStatusCode(String successStatusCode) {
		this.successStatusCode = successStatusCode;
	}
}
