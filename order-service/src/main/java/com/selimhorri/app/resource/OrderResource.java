package com.selimhorri.app.resource;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.selimhorri.app.dto.OrderDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.exception.wrapper.ValidationException;
import com.selimhorri.app.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@Slf4j
@RequiredArgsConstructor
public class OrderResource {
	
	private final OrderService orderService;
	
	@GetMapping
	public ResponseEntity<DtoCollectionResponse<OrderDto>> findAll() {
		log.info("*** OrderDto List, controller; fetch all orders *");
		return ResponseEntity.ok(new DtoCollectionResponse<>(this.orderService.findAll()));
	}
	
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDto> findById(
			@PathVariable("orderId") 
			@NotBlank(message = "Input must not be blank") 
			@Valid final String orderId) {
		log.info("*** OrderDto, resource; fetch order by id *");
		
		try {
			final var id = Integer.parseInt(orderId);
			if (id <= 0) {
				throw new ValidationException("Order ID must be a positive integer");
			}
			return ResponseEntity.ok(this.orderService.findById(id));
		} catch (NumberFormatException e) {
			throw new ValidationException("Order ID must be a valid integer");
		}
	}
	
	@PostMapping
	public ResponseEntity<OrderDto> save(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderDto orderDto) {
		log.info("*** OrderDto, resource; save order *");
		return ResponseEntity.ok(this.orderService.save(orderDto));
	}
	
	@PutMapping
	public ResponseEntity<OrderDto> update(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderDto orderDto) {
		log.info("*** OrderDto, resource; update order *");
		return ResponseEntity.ok(this.orderService.update(orderDto));
	}
	
	@PutMapping("/{orderId}")
	public ResponseEntity<OrderDto> update(
			@PathVariable("orderId")
			@NotBlank(message = "Input must not be blank")
			@Valid final String orderId,
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderDto orderDto) {
		log.info("*** OrderDto, resource; update order with orderId *");
		
		try {
			final var id = Integer.parseInt(orderId);
			if (id <= 0) {
				throw new ValidationException("Order ID must be a positive integer");
			}
			return ResponseEntity.ok(this.orderService.update(id, orderDto));
		} catch (NumberFormatException e) {
			throw new ValidationException("Order ID must be a valid integer");
		}
	}
	
	@DeleteMapping("/{orderId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("orderId") final String orderId) {
		log.info("*** Boolean, resource; delete order by id *");
		
		try {
			final var id = Integer.parseInt(orderId);
			if (id <= 0) {
				throw new ValidationException("Order ID must be a positive integer");
			}
			this.orderService.deleteById(id);
			return ResponseEntity.ok(true);
		} catch (NumberFormatException e) {
			throw new ValidationException("Order ID must be a valid integer");
		}
	}
	
	
	
}










