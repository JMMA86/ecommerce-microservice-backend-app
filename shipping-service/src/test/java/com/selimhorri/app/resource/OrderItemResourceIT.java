package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.OrderItemService;

@WebMvcTest(OrderItemResource.class)
@ActiveProfiles("test")
public class OrderItemResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private OrderItemService orderItemService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testFindAll() throws Exception {
		final List<OrderItemDto> orderItems = Collections.singletonList(this.buildOrderItemDto());
		when(this.orderItemService.findAll()).thenReturn(orderItems);
		
		this.mockMvc.perform(get("/api/shippings"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.collection[0].orderId").value(1))
			.andExpect(jsonPath("$.collection[0].productId").value(1));
	}
	
	@Test
	public void testFindByIdPathVariables() throws Exception {
		final OrderItemDto orderItemDto = this.buildOrderItemDto();
		when(this.orderItemService.findById(any(OrderItemId.class))).thenReturn(orderItemDto);
		
		this.mockMvc.perform(get("/api/shippings/1/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderId").value(1))
			.andExpect(jsonPath("$.productId").value(1));
	}
	
	@Test
	public void testFindByIdPathVariablesInvalidOrderId() throws Exception {
		this.mockMvc.perform(get("/api/shippings/0/1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdPathVariablesInvalidProductId() throws Exception {
		this.mockMvc.perform(get("/api/shippings/1/0"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdPathVariablesInvalidFormat() throws Exception {
		this.mockMvc.perform(get("/api/shippings/abc/1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdRequestBody() throws Exception {
		final OrderItemDto orderItemDto = this.buildOrderItemDto();
		when(this.orderItemService.findById(any(OrderItemId.class))).thenReturn(orderItemDto);
		
		this.mockMvc.perform(get("/api/shippings/find")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(new OrderItemId(1, 1))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderId").value(1))
			.andExpect(jsonPath("$.productId").value(1));
	}
	
	@Test
	public void testSave() throws Exception {
		final OrderItemDto orderItemDto = this.buildOrderItemDto();
		when(this.orderItemService.save(any(OrderItemDto.class))).thenReturn(orderItemDto);
		
		this.mockMvc.perform(post("/api/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderItemDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderId").value(1))
			.andExpect(jsonPath("$.productId").value(1));
	}
	
	@Test
	public void testUpdate() throws Exception {
		final OrderItemDto orderItemDto = this.buildOrderItemDto();
		when(this.orderItemService.update(any(OrderItemDto.class))).thenReturn(orderItemDto);
		
		this.mockMvc.perform(put("/api/shippings")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderItemDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.orderId").value(1))
			.andExpect(jsonPath("$.productId").value(1));
	}
	
	@Test
	public void testDeleteByIdPathVariables() throws Exception {
		doNothing().when(this.orderItemService).deleteById(any(OrderItemId.class));
		
		this.mockMvc.perform(delete("/api/shippings/1/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}
	
	@Test
	public void testDeleteByIdPathVariablesInvalidOrderId() throws Exception {
		this.mockMvc.perform(delete("/api/shippings/-1/1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdPathVariablesInvalidProductId() throws Exception {
		this.mockMvc.perform(delete("/api/shippings/1/-1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdPathVariablesInvalidFormat() throws Exception {
		this.mockMvc.perform(delete("/api/shippings/xyz/1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdRequestBody() throws Exception {
		doNothing().when(this.orderItemService).deleteById(any(OrderItemId.class));
		
		this.mockMvc.perform(delete("/api/shippings/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(new OrderItemId(1, 1))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}
	
	private OrderItemDto buildOrderItemDto() {
		return OrderItemDto.builder()
			.orderId(1)
			.productId(1)
			.orderedQuantity(2)
			.build();
	}
	
}