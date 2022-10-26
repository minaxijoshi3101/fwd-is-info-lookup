package com.fwd.is.facade;

import com.fwd.data.common.home.HomeOwnerInfoResponse;

public interface HomeInsuranceInfoLookupFacade {

	public HomeOwnerInfoResponse getProductStructure(String policyHolderType);
}
