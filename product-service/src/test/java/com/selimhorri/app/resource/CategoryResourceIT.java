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
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.CategoryService;

@WebMvcTest(CategoryResource.class)
@ActiveProfiles("test")
public class CategoryResourceIT {
	
	@Autowired
	private MockMvc mockMvc;
	
	@MockBean
	private CategoryService categoryService;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testFindAll() throws Exception {
		final List<CategoryDto> categories = Collections.singletonList(this.buildCategoryDto());
		when(this.categoryService.findAll()).thenReturn(categories);
		
		this.mockMvc.perform(get("/api/categories"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.collection[0].categoryId").value(1))
			.andExpect(jsonPath("$.collection[0].categoryTitle").value("Test Category"));
	}
	
	@Test
	public void testFindById() throws Exception {
		final CategoryDto categoryDto = this.buildCategoryDto();
		when(this.categoryService.findById(anyInt())).thenReturn(categoryDto);
		
		this.mockMvc.perform(get("/api/categories/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categoryId").value(1))
			.andExpect(jsonPath("$.categoryTitle").value("Test Category"));
	}
	
	@Test
	public void testFindByIdInvalidCategoryId() throws Exception {
		this.mockMvc.perform(get("/api/categories/0"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testFindByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(get("/api/categories/abc"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testSave() throws Exception {
		final CategoryDto categoryDto = this.buildCategoryDto();
		when(this.categoryService.save(any(CategoryDto.class))).thenReturn(categoryDto);
		
		this.mockMvc.perform(post("/api/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(categoryDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categoryId").value(1))
			.andExpect(jsonPath("$.categoryTitle").value("Test Category"));
	}
	
	@Test
	public void testUpdate() throws Exception {
		final CategoryDto categoryDto = this.buildCategoryDto();
		when(this.categoryService.update(any(CategoryDto.class))).thenReturn(categoryDto);
		
		this.mockMvc.perform(put("/api/categories")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(categoryDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categoryId").value(1))
			.andExpect(jsonPath("$.categoryTitle").value("Test Category"));
	}
	
	@Test
	public void testUpdateWithPathVariable() throws Exception {
		final CategoryDto categoryDto = this.buildCategoryDto();
		when(this.categoryService.update(anyInt(), any(CategoryDto.class))).thenReturn(categoryDto);
		
		this.mockMvc.perform(put("/api/categories/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(categoryDto)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.categoryId").value(1))
			.andExpect(jsonPath("$.categoryTitle").value("Test Category"));
	}
	
	@Test
	public void testDeleteById() throws Exception {
		doNothing().when(this.categoryService).deleteById(anyInt());
		
		this.mockMvc.perform(delete("/api/categories/1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").value(true));
	}
	
	@Test
	public void testDeleteByIdInvalidCategoryId() throws Exception {
		this.mockMvc.perform(delete("/api/categories/-1"))
			.andExpect(status().isBadRequest());
	}
	
	@Test
	public void testDeleteByIdInvalidFormat() throws Exception {
		this.mockMvc.perform(delete("/api/categories/xyz"))
			.andExpect(status().isBadRequest());
	}
	
	private CategoryDto buildCategoryDto() {
		return CategoryDto.builder()
			.categoryId(1)
			.categoryTitle("Test Category")
			.imageUrl("test-image.jpg")
			.build();
	}
	
}