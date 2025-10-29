package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.service.OrderService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WebMvcTest(OrderResource.class)
class OrderResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private OrderService orderService;

	@Test
	void testFindAll() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Test Order")
				.orderFee(100.0)
				.build();
		final var orderDtos = List.of(orderDto);

		when(this.orderService.findAll()).thenReturn(orderDtos);

		// When & Then
		this.mockMvc.perform(get("/api/orders"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.collection[0].orderId").value(1))
				.andExpect(jsonPath("$.collection[0].orderDesc").value("Test Order"));
	}

	@Test
	void testFindById() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Test Order")
				.orderFee(100.0)
				.build();

		when(this.orderService.findById(1)).thenReturn(orderDto);

		// When & Then
		this.mockMvc.perform(get("/api/orders/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.orderDesc").value("Test Order"));
	}

	@Test
	void testFindByIdInvalidId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/orders/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a valid integer! ####"));
	}

	@Test
	void testFindByIdNegativeId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/orders/-1"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a positive integer! ####"));
	}

	@Test
	void testFindByIdZeroId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/orders/0"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a positive integer! ####"));
	}

	@Test
	void testFindByIdNotFound() throws Exception {
		// Given
		when(this.orderService.findById(999)).thenThrow(new OrderNotFoundException("Order with id: 999 not found"));

		// When & Then
		this.mockMvc.perform(get("/api/orders/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order with id: 999 not found! ####"));
	}

	@Test
	void testSave() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderDate(LocalDateTime.now())
				.orderDesc("New Order")
				.orderFee(150.0)
				.build();
		final var savedOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(orderDto.getOrderDate())
				.orderDesc(orderDto.getOrderDesc())
				.orderFee(orderDto.getOrderFee())
				.build();

		when(this.orderService.save(any(OrderDto.class))).thenReturn(savedOrderDto);

		// When & Then
		this.mockMvc.perform(post("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.orderDesc").value("New Order"));
	}

	@Test
	void testUpdate() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(LocalDateTime.now())
				.orderDesc("Updated Order")
				.orderFee(200.0)
				.build();

		when(this.orderService.update(any(OrderDto.class))).thenReturn(orderDto);

		// When & Then
		this.mockMvc.perform(put("/api/orders")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.orderDesc").value("Updated Order"));
	}

	@Test
	void testUpdateWithId() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderDate(LocalDateTime.now())
				.orderDesc("Updated Order with ID")
				.orderFee(250.0)
				.build();
		final var updatedOrderDto = OrderDto.builder()
				.orderId(1)
				.orderDate(orderDto.getOrderDate())
				.orderDesc(orderDto.getOrderDesc())
				.orderFee(orderDto.getOrderFee())
				.build();

		when(this.orderService.update(anyInt(), any(OrderDto.class))).thenReturn(updatedOrderDto);

		// When & Then
		this.mockMvc.perform(put("/api/orders/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.orderId").value(1))
				.andExpect(jsonPath("$.orderDesc").value("Updated Order with ID"));
	}

	@Test
	void testUpdateWithIdInvalidId() throws Exception {
		// Given
		final var orderDto = OrderDto.builder()
				.orderDesc("Test")
				.build();

		// When & Then
		this.mockMvc.perform(put("/api/orders/abc")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(orderDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a valid integer! ####"));
	}

	@Test
	void testDeleteById() throws Exception {
		// Given
		doNothing().when(this.orderService).deleteById(1);

		// When & Then
		this.mockMvc.perform(delete("/api/orders/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(true));
	}

	@Test
	void testDeleteByIdInvalidId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/orders/xyz"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a valid integer! ####"));
	}

	@Test
	void testDeleteByIdNegativeId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/orders/-5"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order ID must be a positive integer! ####"));
	}

	@Test
	void testDeleteByIdNotFound() throws Exception {
		// Given
		doThrow(new OrderNotFoundException("Order with id: 999 not found")).when(this.orderService).deleteById(999);

		// When & Then
		this.mockMvc.perform(delete("/api/orders/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Order with id: 999 not found! ####"));
	}

}