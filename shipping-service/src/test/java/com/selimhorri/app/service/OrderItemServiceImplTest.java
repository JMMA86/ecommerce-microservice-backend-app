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

import com.selimhorri.app.domain.OrderItem;
import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.OrderItemNotFoundException;
import com.selimhorri.app.repository.OrderItemRepository;
import com.selimhorri.app.service.impl.OrderItemServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderItemServiceImplTest {

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OrderItemServiceImpl orderItemService;

    private OrderItem orderItem;
    private OrderItemDto orderItemDto;
    private OrderItemId orderItemId;
    private OrderDto orderDto;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        orderItemId = new OrderItemId(1, 1);

        orderDto = new OrderDto();
        orderDto.setOrderId(1);
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setOrderDesc("Test Order");
        orderDto.setOrderFee(99.99);

        productDto = new ProductDto();
        productDto.setProductId(1);
        productDto.setProductTitle("Test Product");
        productDto.setPriceUnit(99.99);

        orderItem = new OrderItem();
        orderItem.setProductId(1);
        orderItem.setOrderId(1);
        orderItem.setOrderedQuantity(2);

        orderItemDto = new OrderItemDto();
        orderItemDto.setProductId(1);
        orderItemDto.setOrderId(1);
        orderItemDto.setOrderedQuantity(2);
        orderItemDto.setProductDto(productDto);
        orderItemDto.setOrderDto(orderDto);
    }

    @Test
    void testFindAll() {
        when(orderItemRepository.findAll()).thenReturn(Arrays.asList(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        List<OrderItemDto> result = orderItemService.findAll();
        assertEquals(1, result.size());
        verify(orderItemRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
    }

    @Test
    void testFindById() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.of(orderItem));
        when(restTemplate.getForObject(anyString(), eq(ProductDto.class))).thenReturn(productDto);
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        OrderItemDto result = orderItemService.findById(orderItemId);
        assertEquals(orderItemDto.getProductId(), result.getProductId());
        verify(orderItemRepository, times(1)).findById(orderItemId);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(ProductDto.class));
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
    }

    @Test
    void testFindByIdNotFound() {
        when(orderItemRepository.findById(orderItemId)).thenReturn(Optional.empty());
        assertThrows(OrderItemNotFoundException.class, () -> orderItemService.findById(orderItemId));
    }

    @Test
    void testSave() {
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        OrderItemDto result = orderItemService.save(orderItemDto);
        assertEquals(orderItemDto.getProductId(), result.getProductId());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void testUpdate() {
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(orderItem);
        OrderItemDto result = orderItemService.update(orderItemDto);
        assertEquals(orderItemDto.getProductId(), result.getProductId());
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
    }

    @Test
    void testDeleteById() {
        orderItemService.deleteById(orderItemId);
        verify(orderItemRepository, times(1)).deleteById(orderItemId);
    }

}