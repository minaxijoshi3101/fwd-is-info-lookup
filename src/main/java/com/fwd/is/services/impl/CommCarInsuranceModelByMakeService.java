package com.fwd.is.services.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.car.CodeTableList;
import com.fwd.data.common.commcar.CommCarModelListResponse;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.ModelByMakeService;

@Service("commCarInsuranceModelByMakeService")
public class CommCarInsuranceModelByMakeService implements ModelByMakeService<String, CommCarModelListResponse> {

	private static final Logger LOG = LogManager.getLogger(CommCarInsuranceModelByMakeService.class);

	private static final String DEFAULT_ERROR_MSG = "11";
	private static final String DEFAULT_ERROR_STATUS = "failed";
	private static final String CAR_MODEL_ERROR = "Car Make is Empty";

	private static final String AUTOLINE = "AutoLine";
	private static final String MAKE = "Make";
	private static final String MODEL = "Model";
	
	@Value("${commcar.product.code}")
	private String productCode;
	
	@Autowired
	private EBaoUtil ebaoUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public CommCarModelListResponse getModelList(String carMakeCode) {
		return getCarModelList(carMakeCode);
	}

	private CommCarModelListResponse getCarModelList(String carMakeCode) {
		try {
			CommCarModelListResponse response = new CommCarModelListResponse();
			if (StringUtils.isBlank(carMakeCode)) {
				return new CommCarModelListResponse(DEFAULT_ERROR_STATUS, DEFAULT_ERROR_MSG, CAR_MODEL_ERROR);
			}
			JSONObject request = new JSONObject();
			request.put(AUTOLINE, productCode);
			request.put(MAKE, carMakeCode);

			String codeListObject = ebaoUtil.commonCodeTableByTableName(MODEL, request.toString());
			if (StringUtils.isNotBlank(codeListObject)) {
				List<CodeTableList> myObjects = objectMapper.readValue(codeListObject,
						new TypeReference<List<CodeTableList>>() {
						});
				response.setModels(myObjects);
			}
			return response;

		} catch (JsonProcessingException e) {
			String err = "Error occured while parsing json. " + e.getMessage();
			LOG.error(err, e);
			throw new JsonParsingException(err, e);
		}
	}
}
