package com.fwd.is.facade;

import com.fwd.data.common.car.CarProductStructureResponse;
import com.fwd.data.common.car.ModelListResponse;

public interface CarInsuranceInfoLookupFacade {

	CarProductStructureResponse getProductStructure();

	ModelListResponse getModelList(String carMakeCode);
}
