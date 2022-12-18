package com.fwd.is.facade.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fwd.data.common.home.HomeOwnerInfoResponse;
import com.fwd.is.facade.HomeInsuranceInfoLookupFacade;
import com.fwd.is.services.ProductStructureService;

@Service("homeInsuranceInfoLookupFacadeImpl")
public class HomeInsuranceInfoLookupFacadeImpl implements HomeInsuranceInfoLookupFacade {

	@Autowired
	private ProductStructureService<String, HomeOwnerInfoResponse> homeInsuranceProductStructureService;

	@Cacheable(value = "productStructure", key = "{#policyHolderType}")
	@Override
	public HomeOwnerInfoResponse getProductStructure(String policyHolderType) {
		return homeInsuranceProductStructureService.getStructure(policyHolderType);
	}
}
