package com.fwd.is;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fwd.data.common.car.CodeTableList;
import com.fwd.data.common.car.MaxVehicleResponse;
import com.fwd.data.common.car.Model;
import com.fwd.data.common.home.HomeCodeTable;
import com.fwd.ia.ebao.adaptor.EBAOAdaptor;
import com.fwd.is.common.constant.Constants;
import com.fwd.is.tasks.ProductStructureTask;

@SpringBootTest(classes = InfoLookupStarter.class)
@TestInstance(Lifecycle.PER_CLASS)
class ProductStructureTaskTest {

	private static final String TEST_TABLE_NAME = "testTableName";

	@Mock
	private EBAOAdaptor ebaoAdaptor;

	@Mock
	private ObjectMapper objectMapper;

	@Mock
	private TypeReference<?> type;

	@Test
	@DisplayName("ProductStructureTask : Code table by table name positive test.")
	void codeTableByTableNamePositiveTest() throws Exception {

		ProductStructureTask task = new ProductStructureTask(TEST_TABLE_NAME, "542421352", ebaoAdaptor, objectMapper,
				Constants.CODE_TABLE_BY_TABLE_NAME, new TypeReference<List<HomeCodeTable>>() {
				});

		JSONObject response = new JSONObject();
		response.put("status", Constants.SUCCESS);
		response.put("status_code", "0");
		when(ebaoAdaptor.codeTableByTableName(anyString(), anyString(), anyString())).thenReturn(response.toString());

		List<?> list = task.call();
		assertNull(list);
	}

	@Test
	@DisplayName("ProductStructureTask : Common code table by table name positive test.")
	void commonCodeTableByTableNamePositiveTest() throws Exception {
		JSONObject request = new JSONObject();
		ProductStructureTask task = new ProductStructureTask(TEST_TABLE_NAME, ebaoAdaptor, objectMapper,
				new TypeReference<List<CodeTableList>>() {
				}, request.toString(), Constants.COMMON_CODE_TABLE_BY_TABLE_NAME);

		JSONObject response = new JSONObject();
		response.put("status", Constants.SUCCESS);
		response.put("status_code", "0");
		when(ebaoAdaptor.commonCodeTableByTableName(anyString(), anyString())).thenReturn(response.toString());

		List<?> list = task.call();
		assertNull(list);
	}

	@Test
	@DisplayName("ProductStructureTask : Get max vehicle age for NB positive test.")
	void getMaxVehicleAgeForNBPositiveTest() throws Exception {
		ProductStructureTask task = new ProductStructureTask(ebaoAdaptor, Constants.GET_MAX_VEHICLE_AGE_FOR_NB);
		ProductStructureTask testTask = new ProductStructureTask();
		MaxVehicleResponse response = new MaxVehicleResponse();
		Model model = new Model();
		model.setMaxVehicleAge(15);
		response.setModel(model);
		when(ebaoAdaptor.getMaxVehicleAgeForNB()).thenReturn(response);

		List<?> list = task.call();
		assertNotNull(list);
		assertNotNull(testTask);
	}

}
