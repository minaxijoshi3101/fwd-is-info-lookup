package com.fwd.is.controllers;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fwd.data.common.car.CarProductStructureResponse;
import com.fwd.data.common.car.ModelListResponse;
import com.fwd.ia.common.constants.Constants;
import com.fwd.is.facade.CarInsuranceInfoLookupFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@ConditionalOnExpression("${api.car.info.lookup.enabled:true}")
@RequestMapping("${api.car.insurance.mapping}")
@Api(value = "Car Insurance Info Lookup Service.", tags = "Car Insurance")
public class CarInsuranceInfoLookupController {

	@Autowired
	private CarInsuranceInfoLookupFacade carInsuranceInfoLookupFacade;

	@ApiOperation(value = "Get Car Info.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved car info."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/car/getProductStructure", produces = "application/json")
	public ResponseEntity<CarProductStructureResponse> getCarInfo() {
		return new ResponseEntity<>(carInsuranceInfoLookupFacade.getProductStructure(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Model List.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved car model list."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "car/{carMakeCode}/getModelList", produces = "application/json")
	public ResponseEntity<ModelListResponse> getModelList(@PathVariable(required = true) String carMakeCode) {
		ModelListResponse modelListResponse = carInsuranceInfoLookupFacade.getModelList(carMakeCode);
		if (!Objects.isNull(modelListResponse) && Constants.SUCCESS.equalsIgnoreCase(modelListResponse.getStatus())) {
			return new ResponseEntity<>(modelListResponse, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(modelListResponse, HttpStatus.BAD_REQUEST);
		}

	}
}