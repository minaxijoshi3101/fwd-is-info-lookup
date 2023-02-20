package com.fwd.is.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.CodeTable;
import com.fwd.data.common.car.CodeTableList;
import com.fwd.data.common.commcar.AgeGroupResponse;
import com.fwd.data.common.commcar.CommCarProductStructureResponse;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.services.ParallelExecutorService;
import com.fwd.is.services.ProductStructureService;
import com.fwd.is.tasks.ProductStructureTask;

@Service("commCarInsuranceProductStructureService")
public class CommCarInsuranceProductStructureService
		implements ProductStructureService<String, CommCarProductStructureResponse> {

	private static final Logger LOG = LogManager.getLogger(CommCarInsuranceProductStructureService.class);

	private static final String AUTO_LINE = "AutoLine";
	private static final String NO_OF_CLAIMS = "noOfClaims";
	private static final String DEMERIT = "demerit";
	private static final String NCD = "ncd";
	private static final String MARITAL_STATUS = "maritalStatus";
	private static final String DRIVING_EXP = "drivingExp";
	private static final String MAKE = "make";

	@Value("${commcar.product.code}")
	private String productCode;

	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Value("#{${car.product.structure.table.names}}")
	private Map<String, String> tableNames;

	@Autowired
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ParallelExecutorService parallelExecutorService;

	@Override
	public CommCarProductStructureResponse getStructure(String param) {
		String reqId = String.format("REQID::%s::REQID", UUID.randomUUID().toString());
		LOG.info("{} START++++ Get Info ++++START", reqId);

		// ###### Parallel Execution - START ######
		Map<String, Callable<?>> tasks = new HashMap<>();

		// Get Make List
		JSONObject request = new JSONObject();
		request.put(AUTO_LINE, productCode);
		tasks.put(tableNames.get(MAKE), new ProductStructureTask(tableNames.get(MAKE), ebaoAdaptor, objectMapper,
				new TypeReference<List<CodeTableList>>() {
				}, request.toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// MaritalStatus
		tasks.put(tableNames.get(MARITAL_STATUS), new ProductStructureTask(tableNames.get(MARITAL_STATUS), ebaoAdaptor,
				objectMapper, new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// Driving Exp List
		tasks.put(tableNames.get(DRIVING_EXP), new ProductStructureTask(tableNames.get(DRIVING_EXP), ebaoAdaptor,
				objectMapper, new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// No. of Claims List
		String productIdVer2 = ebaoAdaptor.getProductIdVer2(productCode);
		tasks.put(tableNames.get(NO_OF_CLAIMS),
				new ProductStructureTask(tableNames.get(NO_OF_CLAIMS), productIdVer2, ebaoAdaptor, objectMapper,
						Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
						}));

		// NCD List
		tasks.put(tableNames.get(NCD), new ProductStructureTask(tableNames.get(NCD), productIdVer2, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
				}));

		// Demerit List
		tasks.put(tableNames.get(DEMERIT), new ProductStructureTask(tableNames.get(DEMERIT), ebaoAdaptor, objectMapper,
				new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// Age By ProductCode
		JSONObject ageByProductReq = new JSONObject();
		ageByProductReq.put("ProductCode", "PV");
		ageByProductReq.put("MinimumAge", 12);
		ageByProductReq.put("MaximumAge", 50);
		tasks.put(Constants.AGE_BY_PRODUCT_CODE,
				new ProductStructureTask(Constants.AGE_BY_PRODUCT_CODE, ebaoAdaptor, objectMapper, new TypeReference<String>() {
				}, ageByProductReq.toString(), Constants.AGE_BY_PRODUCT_CODE));

		Map<String, ?> parallelExecutorResult = parallelExecutorService.executeHeterogenous(tasks);

		// ###### Parallel Execution - END ######

		// Build API Response
		CommCarProductStructureResponse commCarInfoResponse = null;
		try {
			commCarInfoResponse = buildAPIResponse(parallelExecutorResult);
		} catch (JsonProcessingException e) {
			String err = "Error occured while parsing json. " + e.getMessage();
			LOG.error(err, e);
			throw new JsonParsingException(err, e);
		}

		LOG.info("{} END++++ Get Product Structure ++++END", reqId);
		return commCarInfoResponse;
	}

	@SuppressWarnings({ "unchecked" })
	private CommCarProductStructureResponse buildAPIResponse(Map<String, ?> parallelExecutorResult)
			throws JsonProcessingException {
		CommCarProductStructureResponse response = new CommCarProductStructureResponse();
		response.setMakeList((List<CodeTableList>) parallelExecutorResult.get(tableNames.get(MAKE)));
		response.setMaritalStatus((List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(MARITAL_STATUS)));
		response.setDrivingExp((List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(DRIVING_EXP)));
		response.setNoOfClaims((List<CodeTable<Integer>>) parallelExecutorResult.get(tableNames.get(NO_OF_CLAIMS)));
		response.setNcd((List<CodeTable<Integer>>) parallelExecutorResult.get(tableNames.get(NCD)));
		response.setDemerit((List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(DEMERIT)));
		List<String> ageByProductCodeObj = (List<String>) parallelExecutorResult.get(Constants.AGE_BY_PRODUCT_CODE);
		AgeGroupResponse ageGroupResponse = objectMapper.readValue(ageByProductCodeObj.get(0), AgeGroupResponse.class);
		response.setAgeGroup(ageGroupResponse.getModel());
		return response;
	}

	public void setTableNames(Map<String, String> tableNames) {
		this.tableNames = tableNames;
	}

	public void setSuccessStatusCode(String successStatusCode) {
		this.successStatusCode = successStatusCode;
	}
}