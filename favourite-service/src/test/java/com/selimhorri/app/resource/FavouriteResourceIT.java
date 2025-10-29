package com.selimhorri.app.resource;

import static org.mockito.ArgumentMatchers.any;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.constant.AppConstant;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.service.FavouriteService;

import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@WebMvcTest(FavouriteResource.class)
class FavouriteResourceIT {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private FavouriteService favouriteService;

	@Test
	void testFindAll() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(1)
				.likeDate(likeDate)
				.build();
		final var favouriteDtos = List.of(favouriteDto);

		when(this.favouriteService.findAll()).thenReturn(favouriteDtos);

		// When & Then
		this.mockMvc.perform(get("/api/favourites"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.collection[0].userId").value(1))
				.andExpect(jsonPath("$.collection[0].productId").value(1));
	}

	@Test
	void testFindById() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(1, 1, likeDate);
		final var favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(1)
				.likeDate(likeDate)
				.build();

		when(this.favouriteService.findById(favouriteId)).thenReturn(favouriteDto);

		// When & Then
		this.mockMvc.perform(get("/api/favourites/1/1/{likeDate}", likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT))))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.productId").value(1));
	}

	@Test
	void testFindByIdInvalidUserId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/abc/1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### userId must be a valid number! ####"));
	}

	@Test
	void testFindByIdInvalidProductId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/1/abc/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### productId must be a valid number! ####"));
	}

	@Test
	void testFindByIdNegativeUserId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/-1/1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### userId must be positive! ####"));
	}

	@Test
	void testFindByIdNegativeProductId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/1/-1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### productId must be positive! ####"));
	}

	@Test
	void testFindByIdZeroUserId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/0/1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### userId must be positive! ####"));
	}

	@Test
	void testFindByIdZeroProductId() throws Exception {
		// When & Then
		this.mockMvc.perform(get("/api/favourites/1/0/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### productId must be positive! ####"));
	}

	@Test
	void testFindByIdNotFound() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(999, 999, likeDate);
		when(this.favouriteService.findById(favouriteId)).thenThrow(new FavouriteNotFoundException("Favourite with id: 999, 999, " + likeDate + " not found"));

		// When & Then
		this.mockMvc.perform(get("/api/favourites/999/999/{likeDate}", likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT))))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Favourite with id: 999, 999, " + likeDate + " not found! ####"));
	}

	@Test
	void testFindByIdWithRequestBody() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(1, 1, likeDate);
		final var favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(1)
				.likeDate(likeDate)
				.build();

		when(this.favouriteService.findById(favouriteId)).thenReturn(favouriteDto);

		// When & Then
		this.mockMvc.perform(post("/api/favourites/find")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(favouriteId)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.productId").value(1));
	}

	@Test
	void testSave() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(1)
				.likeDate(likeDate)
				.build();

		when(this.favouriteService.save(any(FavouriteDto.class))).thenReturn(favouriteDto);

		// When & Then
		this.mockMvc.perform(post("/api/favourites")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(favouriteDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.productId").value(1));
	}

	@Test
	void testUpdate() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteDto = FavouriteDto.builder()
				.userId(1)
				.productId(1)
				.likeDate(likeDate)
				.build();

		when(this.favouriteService.update(any(FavouriteDto.class))).thenReturn(favouriteDto);

		// When & Then
		this.mockMvc.perform(put("/api/favourites")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(favouriteDto)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.userId").value(1))
				.andExpect(jsonPath("$.productId").value(1));
	}

	@Test
	void testDeleteById() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(1, 1, likeDate);
		doNothing().when(this.favouriteService).deleteById(favouriteId);

		// When & Then
		this.mockMvc.perform(delete("/api/favourites/1/1/{likeDate}", likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT))))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(true));
	}

	@Test
	void testDeleteByIdInvalidUserId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/favourites/abc/1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### userId must be a valid number! ####"));
	}

	@Test
	void testDeleteByIdInvalidProductId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/favourites/1/abc/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### productId must be a valid number! ####"));
	}

	@Test
	void testDeleteByIdNegativeUserId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/favourites/-1/1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### userId must be positive! ####"));
	}

	@Test
	void testDeleteByIdNegativeProductId() throws Exception {
		// When & Then
		this.mockMvc.perform(delete("/api/favourites/1/-1/01-01-2023__12:00:00:000000"))
				.andExpect(status().isBadRequest())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### productId must be positive! ####"));
	}

	@Test
	void testDeleteByIdNotFound() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(999, 999, likeDate);
		doThrow(new FavouriteNotFoundException("Favourite with id: 999, 999, " + likeDate + " not found")).when(this.favouriteService).deleteById(favouriteId);

		// When & Then
		this.mockMvc.perform(delete("/api/favourites/999/999/{likeDate}", likeDate.format(DateTimeFormatter.ofPattern(AppConstant.LOCAL_DATE_TIME_FORMAT))))
				.andExpect(status().isNotFound())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.msg").value("#### Favourite with id: 999, 999, " + likeDate + " not found! ####"));
	}

	@Test
	void testDeleteByIdWithRequestBody() throws Exception {
		// Given
		final var likeDate = LocalDateTime.now();
		final var favouriteId = new FavouriteId(1, 1, likeDate);
		doNothing().when(this.favouriteService).deleteById(favouriteId);

		// When & Then
		this.mockMvc.perform(delete("/api/favourites/delete")
				.contentType(MediaType.APPLICATION_JSON)
				.content(this.objectMapper.writeValueAsString(favouriteId)))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$").value(true));
	}

}