package com.fwd.is.facade.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fwd.data.common.pa.ProductStructureResponse;
import com.fwd.is.facade.PAInsuranceInfoLookupFacade;
import com.fwd.is.services.PAStructureService;

@Service("paInsuranceInfoLookupFacadeImpl")
public class PAInsuranceInfoLookupFacadeImpl implements PAInsuranceInfoLookupFacade {

	@Autowired
	private PAStructureService<String, ProductStructureResponse> paStructureService;
	
	@Cacheable(value = "productStructure", key = "'pa_product_structure'")
	@Override
	public ProductStructureResponse getProductStructure() {
		return paStructureService.getStructure(StringUtils.EMPTY);
	}

}
