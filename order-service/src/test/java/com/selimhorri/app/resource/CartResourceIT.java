package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.exception.wrapper.CartNotFoundException;
import com.selimhorri.app.service.CartService;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WebMvcTest(CartResource.class)
class CartResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private CartService cartService;

	@Test
	void testFindAll() throws Exception {
		// Given
		final var userDto = UserDto.builder()
				.userId(1)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.build();
		final var cartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.userDto(userDto)
				.build();
		final var cartDtos = List.of(cartDto);

		when(this.cartService.findAll()).thenReturn(cartDtos);

		// When & Then
		this.mockMvc.perform(get("/api/carts"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.collection[0].cartId").value(1))
				.andExpect(jsonPath("$.collection[0].user.firstName").value("John"));
	}

	@Test
	void testFindById() throws Exception {
		// Given
		final var userDto = UserDto.builder()
				.userId(1)
				.firstName("John")
				.lastName("Doe")
				.email("john.doe@example.com")
				.build();
		final var cartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.userDto(userDto)
				.build();

		when(this.cartService.findById(1)).thenReturn(cartDto);

		// When & Then
		this.mockMvc.perform(get("/api/carts/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cartId").value(1))
				.andExpect(jsonPath("$.user.firstName").value("John"));
	}

	@Test
	void testFindByIdInvalidId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/carts/abc"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a valid integer! ####"));
	}

	@Test
	void testFindByIdNegativeId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/carts/-1"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a positive integer! ####"));
	}

	@Test
	void testFindByIdZeroId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/carts/0"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a positive integer! ####"));
	}

	@Test
	void testFindByIdNotFound() throws Exception {
		// Given
		when(this.cartService.findById(999)).thenThrow(new CartNotFoundException("Cart with id: 999 not found"));

		// When & Then
		this.mockMvc.perform(get("/api/carts/999"))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart with id: 999 not found! ####"));
	}

	@Test
	void testSave() throws Exception {
		// Given
		final var cartDto = CartDto.builder()
				.userId(1)
				.build();
		final var savedCartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.build();

		when(this.cartService.save(any(CartDto.class))).thenReturn(savedCartDto);

		// When & Then
		this.mockMvc.perform(post("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(cartDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cartId").value(1))
				.andExpect(jsonPath("$.userId").value(1));
	}

	@Test
	void testUpdate() throws Exception {
		// Given
		final var cartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.build();

		when(this.cartService.update(any(CartDto.class))).thenReturn(cartDto);

		// When & Then
		this.mockMvc.perform(put("/api/carts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(cartDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cartId").value(1))
				.andExpect(jsonPath("$.userId").value(1));
	}

	@Test
	void testUpdateWithId() throws Exception {
		// Given
		final var cartDto = CartDto.builder()
				.userId(1)
				.build();
		final var updatedCartDto = CartDto.builder()
				.cartId(1)
				.userId(1)
				.build();

		when(this.cartService.update(anyInt(), any(CartDto.class))).thenReturn(updatedCartDto);

		// When & Then
		this.mockMvc.perform(put("/api/carts/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(cartDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.cartId").value(1))
				.andExpect(jsonPath("$.userId").value(1));
	}

	@Test
	void testUpdateWithIdInvalidId() throws Exception {
		// Given
		final var cartDto = CartDto.builder()
				.userId(1)
				.build();

		// When & Then
		this.mockMvc.perform(put("/api/carts/xyz")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(cartDto)))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a valid integer! ####"));
	}

	@Test
	void testDeleteById() throws Exception {
		// Given
		doNothing().when(this.cartService).deleteById(1);

		// When & Then
		this.mockMvc.perform(delete("/api/carts/1"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(true));
	}

	@Test
	void testDeleteByIdInvalidId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/carts/def"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a valid integer! ####"));
	}

	@Test
	void testDeleteByIdNegativeId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/carts/-3"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Cart ID must be a positive integer! ####"));
	}

	@Test
	void testDeleteByIdNotFound() throws Exception {
		// Given
		doNothing().when(this.cartService).deleteById(888);

		// When & Then
		this.mockMvc.perform(delete("/api/carts/888"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(true));
	}

}