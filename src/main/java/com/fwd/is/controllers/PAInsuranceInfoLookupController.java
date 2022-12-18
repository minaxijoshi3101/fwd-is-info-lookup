package com.fwd.is.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fwd.data.common.pa.ProductStructureResponse;
import com.fwd.is.facade.PAInsuranceInfoLookupFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@ConditionalOnExpression("${api.pa.info.service.enabled:true}")
@RequestMapping("${api.pa.insurance.mapping}")
@Api(value = "Personal Accident Insurance Info Lookup Service.", tags = "Personal Accident Insurance")
public class PAInsuranceInfoLookupController {

	@Autowired
	private PAInsuranceInfoLookupFacade paInsuranceInfoLookupFacade;

	@ApiOperation(value = "Get Product Info.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved product info."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/pa/v1/getProductStructure", produces = "application/json")
	public ResponseEntity<ProductStructureResponse> getProductInfo() {
		return new ResponseEntity<>(paInsuranceInfoLookupFacade.getProductStructure(), HttpStatus.OK);
	}

}