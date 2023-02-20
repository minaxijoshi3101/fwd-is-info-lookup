package com.fwd.is.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fwd.data.common.commcar.CommCarModelListResponse;
import com.fwd.data.common.commcar.CommCarProductStructureResponse;
import com.fwd.data.common.commcar.FinanceCompanyResponse;
import com.fwd.is.facade.CommCarInsuranceInfoLookupFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@ConditionalOnExpression("${api.commcar.info.lookup.enabled:true}")
@RequestMapping("${api.commcar.insurance.mapping}")
@Api(value = "Commercial Car Insurance Info Lookup Service.", tags = "Commercial Car Insurance")
public class CommCarInsuranceInfoLookupController {

	@Autowired
	private CommCarInsuranceInfoLookupFacade commCarInsuranceInfoLookupFacade;

	@ApiOperation(value = "Get Info.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved car info."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/commcar/info", produces = "application/json")
	public ResponseEntity<CommCarProductStructureResponse> getCarInfo() {
		return new ResponseEntity<>(commCarInsuranceInfoLookupFacade.getProductStructure(), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Model List.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved car model list."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/commcar/info/car/{carModel}/model", produces = "application/json")
	public ResponseEntity<CommCarModelListResponse> getModelList(@PathVariable(required = true) String carModel) {
		return new ResponseEntity<>(commCarInsuranceInfoLookupFacade.getModelList(carModel), HttpStatus.OK);
	}

	@ApiOperation(value = "Get Finance Company.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved car info."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/commcar/info/financeCompany", produces = "application/json")
	public ResponseEntity<FinanceCompanyResponse> getFinanceCompany() {
		return new ResponseEntity<>(commCarInsuranceInfoLookupFacade.getFinanceCompany(), HttpStatus.OK);
	}
}