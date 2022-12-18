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
import com.fwd.data.common.car.ModelListData;
import com.fwd.data.common.car.ModelListResponse;
import com.fwd.ia.common.constants.Constants;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.common.utils.EBaoUtil;
import com.fwd.is.services.ModelByMakeService;

@Service("carInsuranceModelByMakeService")
public class CarInsuranceModelByMakeService implements ModelByMakeService<String, ModelListResponse> {

	private static final Logger LOG = LogManager.getLogger(CarInsuranceModelByMakeService.class);

	private static final String DEFAULT_ERROR_MSG = "11";
	private static final String DEFAULT_ERROR_STATUS = "failed";
	private static final String CAR_MODEL_ERROR = "Car Make is Empty";
	private static final String SUCCESS_MESSAGE = "Sucessfully return list of model";

	private static final String AUTOLINE = "AutoLine";
	private static final String PV = "PV";
	private static final String MAKE = "Make";
	private static final String MODEL = "Model";
	
	@Value("${api.success.status.code}")
	private String successStatusCode;

	@Autowired
	private EBaoUtil ebaoUtil;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public ModelListResponse getModelList(String carMakeCode) {
		return getCarModelList(carMakeCode);
	}

	private ModelListResponse getCarModelList(String carMakeCode) {
		try {
			ModelListResponse response = new ModelListResponse();
			if (StringUtils.isBlank(carMakeCode)) {
				return new ModelListResponse(DEFAULT_ERROR_STATUS, DEFAULT_ERROR_MSG, CAR_MODEL_ERROR);
			}
			JSONObject request = new JSONObject();
			request.put(AUTOLINE, PV);
			request.put(MAKE, carMakeCode);

			String codeListObject = ebaoUtil.commonCodeTableByTableName(MODEL, request.toString());
			if (StringUtils.isNotBlank(codeListObject)) {
				List<ModelListData> myObjects = objectMapper.readValue(codeListObject,
						new TypeReference<List<ModelListData>>() {
						});
				response.setData(myObjects);
				response.setMessage(SUCCESS_MESSAGE);
				response.setStatus(Constants.SUCCESS);
				response.setStatusCode(Integer.parseInt(successStatusCode));
			}
			return response;

		} catch (JsonProcessingException e) {
			String err = "Error occured while parsing json. " + e.getMessage();
			LOG.error(err, e);
			throw new JsonParsingException(err, e);
		}
	}

	public void setSuccessStatusCode(String successStatusCode) {
		this.successStatusCode = successStatusCode;
	}

}
