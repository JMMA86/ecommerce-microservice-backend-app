package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.selimhorri.app.domain.Favourite;
import com.selimhorri.app.domain.id.FavouriteId;
import com.selimhorri.app.dto.FavouriteDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.FavouriteNotFoundException;
import com.selimhorri.app.repository.FavouriteRepository;
import com.selimhorri.app.service.impl.FavouriteServiceImpl;

@ExtendWith(MockitoExtension.class)
class FavouriteServiceImplTest {

    @Mock
    private FavouriteRepository favouriteRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FavouriteServiceImpl favouriteService;

    private Favourite favourite;
    private FavouriteDto favouriteDto;
    private FavouriteId favouriteId;
    private UserDto userDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        LocalDateTime likeDate = LocalDateTime.now();

        favouriteId = new FavouriteId(1, 1, likeDate);

        userDto = new UserDto();
        userDto.setUserId(1);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");

        productDto = new ProductDto();
        productDto.setProductId(1);
        productDto.setProductTitle("Test Product");
        productDto.setPriceUnit(99.99);

        favourite = new Favourite();
        favourite.setUserId(1);
        favourite.setProductId(1);
        favourite.setLikeDate(likeDate);

        favouriteDto = new FavouriteDto();
        favouriteDto.setUserId(1);
        favouriteDto.setProductId(1);
        favouriteDto.setLikeDate(likeDate);
        favouriteDto.setUserDto(userDto);
        favouriteDto.setProductDto(productDto);
    }

    @Test
    void testFindAll() {
        when(favouriteRepository.findAll()).thenReturn(Arrays.asList(favourite));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(userDto);
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        List<FavouriteDto> result = favouriteService.findAll();
        assertEquals(1, result.size());
        verify(favouriteRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
    }

    @Test
    void testFindById() {
        when(favouriteRepository.findById(favouriteId)).thenReturn(Optional.of(favourite));
        when(restTemplate.getForObject(anyString(), eq(UserDto.class))).thenReturn(userDto);
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        FavouriteDto result = favouriteService.findById(favouriteId);
        assertEquals(favouriteDto.getUserId(), result.getUserId());
        verify(favouriteRepository, times(1)).findById(favouriteId);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(UserDto.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
    }

    @Test
    void testFindByIdNotFound() {
        when(favouriteRepository.findById(favouriteId)).thenReturn(Optional.empty());
        assertThrows(FavouriteNotFoundException.class, () -> favouriteService.findById(favouriteId));
    }

    @Test
    void testSave() {
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);
        FavouriteDto result = favouriteService.save(favouriteDto);
        assertEquals(favouriteDto.getUserId(), result.getUserId());
        verify(favouriteRepository, times(1)).save(any(Favourite.class));
    }

    @Test
    void testUpdate() {
        when(favouriteRepository.save(any(Favourite.class))).thenReturn(favourite);
        FavouriteDto result = favouriteService.update(favouriteDto);
        assertEquals(favouriteDto.getUserId(), result.getUserId());
        verify(favouriteRepository, times(1)).save(any(Favourite.class));
    }

    @Test
    void testDeleteById() {
        favouriteService.deleteById(favouriteId);
        verify(favouriteRepository, times(1)).deleteById(favouriteId);
    }

}