package com.fwd.is.tasks;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.json.JSONObject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.car.MaxVehicleResponse;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;

public class ProductStructureTask implements Callable<List<?>> {

	private String tableName;
	private String productId;
	private EBAOAdaptor ebaoAdaptor;
	private ObjectMapper objectMapper;
	private TypeReference<?> type;
	private String requestPayload;
	private String ebaoMethodName;

	public ProductStructureTask() {
		super();
	}

	public ProductStructureTask(EBAOAdaptor ebaoAdaptor, String ebaoMethodName) {
		super();
		this.ebaoAdaptor = ebaoAdaptor;
		this.ebaoMethodName = ebaoMethodName;
	}

	public ProductStructureTask(String tableName, String productId, EBAOAdaptor ebaoAdaptor, ObjectMapper objectMapper,
			String ebaoMethodName, TypeReference<?> type) {
		super();
		this.tableName = tableName;
		this.productId = productId;
		this.ebaoAdaptor = ebaoAdaptor;
		this.objectMapper = objectMapper;
		this.ebaoMethodName = ebaoMethodName;
		this.type = type;
	}

	public ProductStructureTask(String tableName, EBAOAdaptor ebaoAdaptor, ObjectMapper objectMapper,
			TypeReference<?> type, String requestPayload, String ebaoMethodName) {
		super();
		this.tableName = tableName;
		this.ebaoAdaptor = ebaoAdaptor;
		this.objectMapper = objectMapper;
		this.type = type;
		this.requestPayload = requestPayload;
		this.ebaoMethodName = ebaoMethodName;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<?> call() throws Exception {
		List result = Collections.emptyList();
		if (Constants.CODE_TABLE_BY_TABLE_NAME.equals(ebaoMethodName)) {
			result = (List) objectMapper.readValue(codeTableByTableName(tableName, productId), type);

		} else if (Constants.COMMON_CODE_TABLE_BY_TABLE_NAME.equals(ebaoMethodName)) {
			result = (List) objectMapper.readValue(commonCodeTableByTableName(tableName, requestPayload), type);

		} else if (Constants.GET_MAX_VEHICLE_AGE_FOR_NB.equals(ebaoMethodName)) {
			MaxVehicleResponse maxVehicleAgeResponse = ebaoAdaptor.getMaxVehicleAgeForNB();
			return Arrays.asList(maxVehicleAgeResponse.getModel().getMaxVehicleAge());
		}
		return result;
	}

	private String codeTableByTableName(String tableName, String productId) {
		return ebaoAdaptor.codeTableByTableName(tableName, productId, new JSONObject().toString());
	}

	private String commonCodeTableByTableName(String tableName, String payload) {
		return ebaoAdaptor.commonCodeTableByTableName(tableName, payload);
	}
}
