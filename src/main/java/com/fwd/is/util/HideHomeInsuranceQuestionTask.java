package com.fwd.is.util;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class HideHomeInsuranceQuestionTask implements Callable<List<?>> {

	private HomeInsuranceProductStructureUtil productStructureUtil;

	public HideHomeInsuranceQuestionTask() {
		super();
	}

	public HideHomeInsuranceQuestionTask(HomeInsuranceProductStructureUtil productStructureUtil) {
		super();
		this.productStructureUtil = productStructureUtil;
	}

	@Override
	public List<?> call() throws Exception {
		return Arrays.asList(productStructureUtil.getHideHomeInsuranceQuestion());
	}

}
