package com.fwd.is.util;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fwd.is.common.exceptions.IntegrationServiceException;

@Component
public class HomeInsuranceProductStructureUtil {

	private static final Logger LOG = LogManager.getLogger(HomeInsuranceProductStructureUtil.class);
	private static final String ERROR_MESSAGE = "Exception occurred while making a ajax call. URL: %s";

	@Value("${hide.home.insurance.question.url}")
	private String hideHomeInsuranceQuestionURL;

	public void setHideHomeInsuranceQuestionURL(String hideHomeInsuranceQuestionURL) {
		this.hideHomeInsuranceQuestionURL = hideHomeInsuranceQuestionURL;
	}

	@Autowired
	private RestTemplate restTemplate;

	public boolean getHideHomeInsuranceQuestion() {
		try {
			ResponseEntity<String> responseEntity = restTemplate.getForEntity(hideHomeInsuranceQuestionURL,
					String.class);
			if (responseEntity.getStatusCode().is2xxSuccessful()) {
				JSONObject response = getJsonFromString(responseEntity.getBody());
				return getValueFromJson(response);
			} else {
				throw new IntegrationServiceException(String.format(ERROR_MESSAGE, hideHomeInsuranceQuestionURL));
			}

		} catch (JSONException e) {
			String err = "Exception occured while parsing json.";
			LOG.error(err, e);
			throw new IntegrationServiceException(err, e);
		} catch (IntegrationServiceException e) {
			LOG.error(e.getMessage(), e);
			throw e;
		} catch (Exception e) {
			String err = String.format(ERROR_MESSAGE, hideHomeInsuranceQuestionURL);
			LOG.error(err, e);
			throw new IntegrationServiceException(err, e);
		}

	}

	private boolean getValueFromJson(JSONObject response) {
		if (Objects.nonNull(response)) {
			return response.getBoolean("hide_home_insurance_question");
		}
		return false;
	}

	private JSONObject getJsonFromString(String response) {
		if (StringUtils.isNotBlank(response)) {
			return new JSONObject(response);
		}
		return null;
	}
}
