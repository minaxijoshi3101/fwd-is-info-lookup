package com.fwd.is.facade.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fwd.data.common.car.CarProductStructureResponse;
import com.fwd.data.common.car.ModelListResponse;
import com.fwd.is.facade.CarInsuranceInfoLookupFacade;
import com.fwd.is.services.ModelByMakeService;
import com.fwd.is.services.ProductStructureService;

@Service("carInsuranceInfoLookupFacade")
public class CarInsuranceInfoLookupFacadeImpl implements CarInsuranceInfoLookupFacade {

	@Autowired
	private ProductStructureService<String, CarProductStructureResponse> carInsuranceProductStructureService;

	@Autowired
	private ModelByMakeService<String, ModelListResponse> carInsuranceModelByMakeService;

	@Cacheable(value = "productStructure", key = "'car_product_structure'")
	@Override
	public CarProductStructureResponse getProductStructure() {
		return carInsuranceProductStructureService.getStructure(StringUtils.EMPTY);
	}

	@Cacheable(value = "modelList", key = "{'carMakeCode_'.concat(#carMakeCode)}")
	@Override
	public ModelListResponse getModelList(String carMakeCode) {
		return carInsuranceModelByMakeService.getModelList(carMakeCode);
	}

}
