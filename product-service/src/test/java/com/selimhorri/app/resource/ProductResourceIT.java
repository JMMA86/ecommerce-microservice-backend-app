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
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.ProductService;

@WebMvcTest(ProductResource.class)
@ActiveProfiles("test")
public class ProductResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private ProductService productService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testFindAll() throws Exception {
		final List<ProductDto> products = Collections.singletonList(this.buildProductDto());
		when(this.productService.findAll()).thenReturn(products);
		
		this.mockMvc.perform(get("/api/products"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.collection[0].productId").value(1))
			.andExpect(jsonPath("$.collection[0].productTitle").value("Test Product"));
	}
	
	@Test
	public void testFindById() throws Exception {
		final ProductDto productDto = this.buildProductDto();
		when(this.productService.findById(anyInt())).thenReturn(productDto);
		
		this.mockMvc.perform(get("/api/products/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.productTitle").value("Test Product"));
	}
	
	@Test
	public void testFindByIdInvalidProductId() throws Exception {
		this.mockMvc.perform(get("/api/products/0"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(get("/api/products/abc"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSave() throws Exception {
		final ProductDto productDto = this.buildProductDto();
		when(this.productService.save(any(ProductDto.class))).thenReturn(productDto);
		
		this.mockMvc.perform(post("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(productDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.productTitle").value("Test Product"));
	}
	
	@Test
	public void testUpdate() throws Exception {
		final ProductDto productDto = this.buildProductDto();
		when(this.productService.update(any(ProductDto.class))).thenReturn(productDto);
		
		this.mockMvc.perform(put("/api/products")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(productDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.productTitle").value("Test Product"));
	}
	
	@Test
	public void testUpdateWithPathVariable() throws Exception {
		final ProductDto productDto = this.buildProductDto();
		when(this.productService.update(anyInt(), any(ProductDto.class))).thenReturn(productDto);
		
		this.mockMvc.perform(put("/api/products/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(productDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.productId").value(1))
			.andExpect(jsonPath("$.productTitle").value("Test Product"));
	}
	
	@Test
	public void testDeleteById() throws Exception {
		doNothing().when(this.productService).deleteById(anyInt());
		
		this.mockMvc.perform(delete("/api/products/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}
	
	@Test
	public void testDeleteByIdInvalidProductId() throws Exception {
		this.mockMvc.perform(delete("/api/products/-1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(delete("/api/products/xyz"))
			.andExpect(status().isBadRequest());
	}
	
	private ProductDto buildProductDto() {
		return ProductDto.builder()
			.productId(1)
			.productTitle("Test Product")
			.imageUrl("test-product.jpg")
			.sku("TEST-001")
			.priceUnit(29.99)
			.quantity(10)
			.build();
	}
	
}