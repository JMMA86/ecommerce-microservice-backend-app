package com.selimhorri.app.resource;

import com.selimhorri.app.exception.wrapper.ValidationException;

import javax.validation.Valid;
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

import com.selimhorri.app.domain.id.OrderItemId;
import com.selimhorri.app.dto.OrderItemDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.OrderItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/shippings")
@Slf4j
@RequiredArgsConstructor
public class OrderItemResource {
	
	private final OrderItemService orderItemService;
	
	@GetMapping
	public ResponseEntity<DtoCollectionResponse<OrderItemDto>> findAll() {
		log.info("*** OrderItemDto List, controller; fetch all orderItems *");
		return ResponseEntity.ok(new DtoCollectionResponse<>(this.orderItemService.findAll()));
	}
	
	@GetMapping("/{orderId}/{productId}")
	public ResponseEntity<OrderItemDto> findById(
			@PathVariable("orderId") final String orderId, 
			@PathVariable("productId") final String productId) {
		log.info("*** OrderItemDto, resource; fetch orderItem by id *");
		try {
			final int oId = Integer.parseInt(orderId.strip());
			final int pId = Integer.parseInt(productId.strip());
			if (oId <= 0 || pId <= 0) {
				throw new ValidationException("Order ID and Product ID must be positive integers");
			}
			return ResponseEntity.ok(this.orderItemService.findById(new OrderItemId(oId, pId)));
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid order ID or product ID format");
		}
	}
	
	@GetMapping("/find")
	public ResponseEntity<OrderItemDto> findById(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderItemId orderItemId) {
		log.info("*** OrderItemDto, resource; fetch orderItem by id *");
		return ResponseEntity.ok(this.orderItemService.findById(orderItemId));
	}
	
	@PostMapping
	public ResponseEntity<OrderItemDto> save(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderItemDto orderItemDto) {
		log.info("*** OrderItemDto, resource; save orderItem *");
		return ResponseEntity.ok(this.orderItemService.save(orderItemDto));
	}
	
	@PutMapping
	public ResponseEntity<OrderItemDto> update(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderItemDto orderItemDto) {
		log.info("*** OrderItemDto, resource; update orderItem *");
		return ResponseEntity.ok(this.orderItemService.update(orderItemDto));
	}
	
	@DeleteMapping("/{orderId}/{productId}")
	public ResponseEntity<Boolean> deleteById(
			@PathVariable("orderId") final String orderId, 
			@PathVariable("productId") final String productId) {
		log.info("*** Boolean, resource; delete orderItem by id *");
		try {
			final int oId = Integer.parseInt(orderId.strip());
			final int pId = Integer.parseInt(productId.strip());
			if (oId <= 0 || pId <= 0) {
				throw new ValidationException("Order ID and Product ID must be positive integers");
			}
			this.orderItemService.deleteById(new OrderItemId(oId, pId));
			return ResponseEntity.ok(true);
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid order ID or product ID format");
		}
	}
	
	@DeleteMapping("/delete")
	public ResponseEntity<Boolean> deleteById(
			@RequestBody 
			@NotNull(message = "Input must not be NULL") 
			@Valid final OrderItemId orderItemId) {
		log.info("*** Boolean, resource; delete orderItem by id *");
		this.orderItemService.deleteById(orderItemId);
		return ResponseEntity.ok(true);
	}
	
	
	
}










