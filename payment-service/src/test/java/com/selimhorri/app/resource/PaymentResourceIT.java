package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.PaymentService;

@WebMvcTest(PaymentResource.class)
@ActiveProfiles("test")
public class PaymentResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private PaymentService paymentService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testFindAll() throws Exception {
		final List<PaymentDto> payments = Collections.singletonList(this.buildPaymentDto());
		when(this.paymentService.findAll()).thenReturn(payments);
		
		this.mockMvc.perform(get("/api/payments"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.collection[0].paymentId").value(1))
			.andExpect(jsonPath("$.collection[0].isPayed").value(true));
	}
	
	@Test
	public void testFindById() throws Exception {
		final PaymentDto paymentDto = this.buildPaymentDto();
		when(this.paymentService.findById(anyInt())).thenReturn(paymentDto);
		
		this.mockMvc.perform(get("/api/payments/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paymentId").value(1))
			.andExpect(jsonPath("$.isPayed").value(true));
	}
	
	@Test
	public void testFindByIdInvalidPaymentId() throws Exception {
		this.mockMvc.perform(get("/api/payments/0"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(get("/api/payments/abc"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSave() throws Exception {
		final PaymentDto paymentDto = this.buildPaymentDto();
		when(this.paymentService.save(any(PaymentDto.class))).thenReturn(paymentDto);
		
		this.mockMvc.perform(post("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(paymentDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paymentId").value(1))
			.andExpect(jsonPath("$.isPayed").value(true));
	}
	
	@Test
	public void testUpdate() throws Exception {
		final PaymentDto paymentDto = this.buildPaymentDto();
		when(this.paymentService.update(any(PaymentDto.class))).thenReturn(paymentDto);
		
		this.mockMvc.perform(put("/api/payments")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(paymentDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.paymentId").value(1))
			.andExpect(jsonPath("$.isPayed").value(true));
	}
	
	@Test
	public void testDeleteById() throws Exception {
		doNothing().when(this.paymentService).deleteById(anyInt());
		
		this.mockMvc.perform(delete("/api/payments/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}
	
	@Test
	public void testDeleteByIdInvalidPaymentId() throws Exception {
		this.mockMvc.perform(delete("/api/payments/-1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(delete("/api/payments/xyz"))
			.andExpect(status().isBadRequest());
	}
	
	private PaymentDto buildPaymentDto() {
		return PaymentDto.builder()
			.paymentId(1)
			.isPayed(true)
			.paymentStatus(PaymentStatus.COMPLETED)
			.build();
	}
	
}