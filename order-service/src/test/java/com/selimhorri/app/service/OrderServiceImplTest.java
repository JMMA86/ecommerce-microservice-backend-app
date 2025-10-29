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

import com.selimhorri.app.domain.Cart;
import com.selimhorri.app.domain.Order;
import com.selimhorri.app.dto.CartDto;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.exception.wrapper.OrderNotFoundException;
import com.selimhorri.app.repository.OrderRepository;
import com.selimhorri.app.service.impl.OrderServiceImpl;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;
    private OrderDto orderDto;
    private Cart cart;
    private CartDto cartDto;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setCartId(1);
        cart.setUserId(1);

        cartDto = new CartDto();
        cartDto.setCartId(1);
        cartDto.setUserId(1);

        order = new Order();
        order.setOrderId(1);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderDesc("Test Order");
        order.setOrderFee(99.99);
        order.setCart(cart);

        orderDto = new OrderDto();
        orderDto.setOrderId(1);
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setOrderDesc("Test Order");
        orderDto.setOrderFee(99.99);
        orderDto.setCartDto(cartDto);
    }

    @Test
    void testFindAll() {
        when(orderRepository.findAll()).thenReturn(Arrays.asList(order));
        List<OrderDto> result = orderService.findAll();
        assertEquals(1, result.size());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        OrderDto result = orderService.findById(1);
        assertEquals(orderDto.getOrderId(), result.getOrderId());
        verify(orderRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        when(orderRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.findById(1));
    }

    @Test
    void testSave() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderDto result = orderService.save(orderDto);
        assertEquals(orderDto.getOrderId(), result.getOrderId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdate() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderDto result = orderService.update(orderDto);
        assertEquals(orderDto.getOrderId(), result.getOrderId());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateWithId() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        OrderDto result = orderService.update(1, orderDto);
        assertEquals(orderDto.getOrderId(), result.getOrderId());
        verify(orderRepository, times(1)).findById(1);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testDeleteById() {
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        orderService.deleteById(1);
        verify(orderRepository, times(1)).delete(any(Order.class));
    }

}