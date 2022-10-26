package com.fwd.is.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fwd.data.common.home.HomeOwnerInfoResponse;
import com.fwd.is.facade.HomeInsuranceInfoLookupFacade;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@ConditionalOnExpression("${api.home.info.lookup.enabled:true}")
@RequestMapping("${api.home.insurance.mapping}")
@Api(value = "Home Insurance Info Lookup Service.", tags = "Home Insurance")
public class HomeInsuranceInfoLookupController {

	@Autowired
	private HomeInsuranceInfoLookupFacade homeInsuranceInfoLookupFacade;

	@ApiOperation(value = "Get Home Owner Info.")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Successfully retrieved home owner info."),
			@ApiResponse(code = 401, message = "You are not authorized to view the resource"),
			@ApiResponse(code = 403, message = "Accessing the resource you were trying to reach is forbidden"),
			@ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
			@ApiResponse(code = 500, message = "Something went wrong. Please try after some time.") })
	@GetMapping(value = "/home/info/{policyHolderType}", produces = "application/json")
	public ResponseEntity<HomeOwnerInfoResponse> getHomeOwnerInfo(
			@PathVariable(required = true) String policyHolderType) {

		return new ResponseEntity<>(homeInsuranceInfoLookupFacade.getProductStructure(policyHolderType), HttpStatus.OK);
	}
}