package com.fwd.is.facade;

import com.fwd.data.common.home.HomeOwnerInfoResponse;

public interface HomeInsuranceInfoLookupFacade {

	HomeOwnerInfoResponse getProductStructure(String policyHolderType);
}
