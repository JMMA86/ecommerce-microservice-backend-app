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

import com.selimhorri.app.domain.Payment;
import com.selimhorri.app.domain.PaymentStatus;
import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.PaymentDto;
import com.selimhorri.app.exception.wrapper.PaymentNotFoundException;
import com.selimhorri.app.repository.PaymentRepository;
import com.selimhorri.app.service.impl.PaymentServiceImpl;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment;
    private PaymentDto paymentDto;
    private OrderDto orderDto;

    @BeforeEach
    void setUp() {
        orderDto = new OrderDto();
        orderDto.setOrderId(1);
        orderDto.setOrderDate(LocalDateTime.now());
        orderDto.setOrderDesc("Test Order");
        orderDto.setOrderFee(99.99);

        payment = new Payment();
        payment.setPaymentId(1);
        payment.setOrderId(1);
        payment.setIsPayed(true);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);

        paymentDto = new PaymentDto();
        paymentDto.setPaymentId(1);
        paymentDto.setIsPayed(true);
        paymentDto.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentDto.setOrderDto(orderDto);
    }

    @Test
    void testFindAll() {
        when(paymentRepository.findAll()).thenReturn(Arrays.asList(payment));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        List<PaymentDto> result = paymentService.findAll();
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findAll();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
    }

    @Test
    void testFindById() {
        when(paymentRepository.findById(1)).thenReturn(Optional.of(payment));
        when(restTemplate.getForObject(anyString(), eq(OrderDto.class))).thenReturn(orderDto);
        PaymentDto result = paymentService.findById(1);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).findById(1);
        verify(restTemplate, times(1)).getForObject(anyString(), eq(OrderDto.class));
    }

    @Test
    void testFindByIdNotFound() {
        when(paymentRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(PaymentNotFoundException.class, () -> paymentService.findById(1));
    }

    @Test
    void testSave() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        PaymentDto result = paymentService.save(paymentDto);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdate() {
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        PaymentDto result = paymentService.update(paymentDto);
        assertEquals(paymentDto.getPaymentId(), result.getPaymentId());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testDeleteById() {
        paymentService.deleteById(1);
        verify(paymentRepository, times(1)).deleteById(1);
    }

}