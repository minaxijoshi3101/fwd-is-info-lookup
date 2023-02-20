package com.fwd.is.facade.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fwd.data.common.commcar.CommCarModelListResponse;
import com.fwd.data.common.commcar.CommCarProductStructureResponse;
import com.fwd.data.common.commcar.FinanceCompanyResponse;
import com.fwd.is.facade.CommCarInsuranceInfoLookupFacade;
import com.fwd.is.services.LookupService;
import com.fwd.is.services.ModelByMakeService;
import com.fwd.is.services.ProductStructureService;

@Service("commCarInsuranceInfoLookupFacade")
public class CommCarInsuranceInfoLookupFacadeImpl implements CommCarInsuranceInfoLookupFacade {

	@Autowired
	private ProductStructureService<String, CommCarProductStructureResponse> commCarInsuranceProductStructureService;

	@Autowired
	private ModelByMakeService<String, CommCarModelListResponse> commCarInsuranceModelByMakeService;

	@Autowired
	private LookupService<String, FinanceCompanyResponse> commCarInsuranceFinanceCompanyService;

	@Cacheable(value = "productStructure", key = "'commcar_product_structure'")
	@Override
	public CommCarProductStructureResponse getProductStructure() {
		return commCarInsuranceProductStructureService.getStructure(StringUtils.EMPTY);
	}
	
	@Cacheable(value = "modelList", key = "{'commcar_carModel_'.concat(#carModel)}")
	@Override
	public CommCarModelListResponse getModelList(String carModel) {
		return commCarInsuranceModelByMakeService.getModelList(carModel);
	}
	
	@Cacheable(value = "productStructure", key = "'commcar_finance_company_list'")
	@Override
	public FinanceCompanyResponse getFinanceCompany() {
		return commCarInsuranceFinanceCompanyService.getFinanceCompany(StringUtils.EMPTY);
	}

}
