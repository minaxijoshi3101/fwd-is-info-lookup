package com.fwd.is.tasks;

import java.util.concurrent.Callable;

import com.fwd.is.common.utils.EBaoUtil;

public class DiscountRateFromCodeTask implements Callable<Double> {

	private String tableName;

	private String productCode;

	private EBaoUtil eBaoUtil;

	public DiscountRateFromCodeTask() {
		super();
	}

	public DiscountRateFromCodeTask(String tableName, String productCode, EBaoUtil eBaoUtil) {
		super();
		this.tableName = tableName;
		this.productCode = productCode;
		this.eBaoUtil = eBaoUtil;
	}

	@Override
	public Double call() throws Exception {
		return eBaoUtil.getDiscountRateFromCode(tableName, productCode);
	}

}
