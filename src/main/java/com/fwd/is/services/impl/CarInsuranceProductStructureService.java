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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.CodeTable;
import com.fwd.data.common.car.CarProductStructureResponse;
import com.fwd.data.common.car.CarStructureData;
import com.fwd.data.common.car.CountriesList;
import com.fwd.data.common.car.CodeTableList;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;
import com.fwd.is.common.services.ParallelExecutorService;
import com.fwd.is.common.utils.TraversalUtil;
import com.fwd.is.services.ProductStructureService;
import com.fwd.is.tasks.ProductStructureTask;
import com.fwd.is.util.ParseFileContentTask;

@Service("carInsuranceProductStructureService")
public class CarInsuranceProductStructureService
		implements ProductStructureService<String, CarProductStructureResponse> {

	private static final Logger LOG = LogManager.getLogger(CarInsuranceProductStructureService.class);

	private static final String PV = "PV";
	private static final String AUTO_LINE = "AutoLine";

	private static final String FINANCE = "finance";
	private static final String DEMERIT = "demerit";
	private static final String NCD = "ncd";
	private static final String NO_OF_CLAIMS = "noOfClaims";
	private static final String DRIVING_EXP = "drivingExp";
	private static final String MARITAL_STATUS = "maritalStatus";
	private static final String MAKE = "make";
	private static final String COUNTRIES = "countries";

	@Value("${car.product.code}")
	private String carProductCode;

	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Value("#{${car.product.structure.table.names}}")
	private Map<String, String> tableNames;

	@Value("${contries.list.file.path}")
	private String countriesFilePath;

	@Autowired
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TraversalUtil traversalUtil;

	@Autowired
	private ParallelExecutorService parallelExecutorService;

	@Override
	public CarProductStructureResponse getStructure(String param) {
		String reqId = String.format("REQID::%s::REQID", UUID.randomUUID().toString());
		LOG.info("{} START++++ Get Product Structure ++++START", reqId);

		// ###### Parallel Execution - START ######
		Map<String, Callable<?>> tasks = new HashMap<>();

		// 1. Get Make List
		JSONObject request = new JSONObject();
		request.put(AUTO_LINE, PV);
		tasks.put(tableNames.get(MAKE), new ProductStructureTask(tableNames.get(MAKE), ebaoAdaptor, objectMapper,
				new TypeReference<List<CodeTableList>>() {
				}, request.toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// 2. Marital Status
		tasks.put(tableNames.get(MARITAL_STATUS), new ProductStructureTask(tableNames.get(MARITAL_STATUS), ebaoAdaptor,
				objectMapper, new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// 3. Driving Exp List
		tasks.put(tableNames.get(DRIVING_EXP), new ProductStructureTask(tableNames.get(DRIVING_EXP), ebaoAdaptor,
				objectMapper, new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// 4. No. of Claims List
		String productIdVer6 = ebaoAdaptor.getProductIdVer6(carProductCode);
		tasks.put(tableNames.get(NO_OF_CLAIMS),
				new ProductStructureTask(tableNames.get(NO_OF_CLAIMS), productIdVer6, ebaoAdaptor, objectMapper,
						Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
						}));

		// 5. NCD List
		String productIdVer5 = ebaoAdaptor.getProductIdVer5(carProductCode);
		tasks.put(tableNames.get(NCD), new ProductStructureTask(tableNames.get(NCD), productIdVer5, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
				}));

		// 6. Demerit List
		tasks.put(tableNames.get(DEMERIT), new ProductStructureTask(tableNames.get(DEMERIT), ebaoAdaptor, objectMapper,
				new TypeReference<List<CodeTable<String>>>() {
				}, new JSONObject().toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME));

		// 7. Finance Company List
		tasks.put(tableNames.get(FINANCE), new ProductStructureTask(tableNames.get(FINANCE), productIdVer6, ebaoAdaptor,
				objectMapper, Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<CodeTable<Integer>>>() {
				}));

		// 8. EBAO Max Vehicle maxVehicleAgeForNB
		tasks.put(Constants.GET_MAX_VEHICLE_AGE_FOR_NB,
				new ProductStructureTask(ebaoAdaptor, Constants.GET_MAX_VEHICLE_AGE_FOR_NB));

		// 9. Country List
		tasks.put(COUNTRIES, new ParseFileContentTask(countriesFilePath, new TypeReference<List<CountriesList>>() {
		}, objectMapper));

		Map<String, ?> parallelExecutorResult = parallelExecutorService.executeHeterogenous(tasks);

		// ###### Parallel Execution - END ######

		// Build API Response
		CarProductStructureResponse carInfoResponse = buildAPIResponse(parallelExecutorResult);

		LOG.info("{} END++++ Get Product Structure ++++END", reqId);
		return carInfoResponse;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CarProductStructureResponse buildAPIResponse(Map<String, ?> parallelExecutorResult) {
		CarProductStructureResponse carInfoResponse = new CarProductStructureResponse();
		carInfoResponse.setStatus(Constants.SUCCESS);
		carInfoResponse.setStatusCode(Integer.parseInt(successStatusCode));
		carInfoResponse.setMessage("Successfully return product structure");

		CarStructureData data = new CarStructureData();
		data.setMakeList((List<CodeTableList>) parallelExecutorResult.get(tableNames.get(MAKE)));
		data.setMaritalStatusList((List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(MARITAL_STATUS)));
		data.setDrivingExperienceList(
				(List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(DRIVING_EXP)));
		data.setNoOfClaimList(traversalUtil.findNoOfClaimList(
				(List<CodeTable<Integer>>) parallelExecutorResult.get(tableNames.get(NO_OF_CLAIMS))));
		data.setNcdList((List<CodeTable<Integer>>) parallelExecutorResult.get(tableNames.get(NCD)));
		data.setDemeritList((List<CodeTable<String>>) parallelExecutorResult.get(tableNames.get(DEMERIT)));
		data.setFinanceCompanyList((List<CodeTable<Integer>>) parallelExecutorResult.get(tableNames.get(FINANCE)));
		data.setCountriesList((List<CountriesList>) parallelExecutorResult.get(COUNTRIES));
		data.setMaxVehicleAge((int) ((List) parallelExecutorResult.get(Constants.GET_MAX_VEHICLE_AGE_FOR_NB)).get(0));
		carInfoResponse.setData(data);
		return carInfoResponse;
	}

	public void setTableNames(Map<String, String> tableNames) {
		this.tableNames = tableNames;
	}

	public void setSuccessStatusCode(String successStatusCode) {
		this.successStatusCode = successStatusCode;
	}
}