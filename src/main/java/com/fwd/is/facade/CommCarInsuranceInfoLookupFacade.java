package com.fwd.is.facade;

import com.fwd.data.common.commcar.CommCarModelListResponse;
import com.fwd.data.common.commcar.CommCarProductStructureResponse;
import com.fwd.data.common.commcar.FinanceCompanyResponse;

public interface CommCarInsuranceInfoLookupFacade {

	CommCarProductStructureResponse getProductStructure();

	CommCarModelListResponse getModelList(String carModel);
	
	FinanceCompanyResponse getFinanceCompany();
}
