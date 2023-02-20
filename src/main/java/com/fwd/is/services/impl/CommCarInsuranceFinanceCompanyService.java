package com.fwd.is.services.impl;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.CodeTable;
import com.fwd.data.common.commcar.FinanceCompanyResponse;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.exceptions.JsonParsingException;
import com.fwd.is.services.LookupService;

@Service("commCarInsuranceFinanceCompanyService")
public class CommCarInsuranceFinanceCompanyService implements LookupService<String, FinanceCompanyResponse> {

	private static final Logger LOG = LogManager.getLogger(CommCarInsuranceFinanceCompanyService.class);

	@Value("${commcar.product.code}")
	private String productCode;

	@Autowired
	private EBAOAdaptor ebaoAdaptor;

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public FinanceCompanyResponse getFinanceCompany(String param) {
		String reqId = String.format("REQID::%s::REQID", UUID.randomUUID().toString());
		LOG.info("{} START++++ Get Finance Comapany ++++START", reqId);
		FinanceCompanyResponse financeCompanies = new FinanceCompanyResponse();
		try {
			String productId = ebaoAdaptor.getProductIdVer2(productCode);
			String codeTableResult = ebaoAdaptor.codeTableByTableName("PolicyRiskVEHICLEFinanceCompany", productId,
					new JSONObject().toString());

			List<CodeTable<Integer>> ebaoFinanceCompanies = objectMapper.readValue(codeTableResult,
					new TypeReference<List<CodeTable<Integer>>>() {
					});
			financeCompanies.setFinanceCompanies(ebaoFinanceCompanies);
		} catch (JsonProcessingException e) {
			String err = "Error occured while parsing json. " + e.getMessage();
			LOG.error(err, e);
			throw new JsonParsingException(err, e);
		}
		return financeCompanies;
	}
	
	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}
}