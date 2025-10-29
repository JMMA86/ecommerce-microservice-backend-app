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

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(value = {"/api/users"})
@Slf4j
@RequiredArgsConstructor
public class UserResource {
	
	private final UserService userService;
	
	@GetMapping
	public ResponseEntity<DtoCollectionResponse<UserDto>> findAll() {
		log.info("*** UserDto List, controller; fetch all users *");
		return ResponseEntity.ok(new DtoCollectionResponse<>(this.userService.findAll()));
	}
	
	@GetMapping("/{userId}")
	public ResponseEntity<UserDto> findById(@PathVariable("userId") final String userId) {
		log.info("*** UserDto, resource; fetch user by id *");
		try {
			final int id = Integer.parseInt(userId.strip());
			if (id <= 0) {
				throw new ValidationException("User ID must be a positive integer");
			}
			return ResponseEntity.ok(this.userService.findById(id));
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid user ID format");
		}
	}
	
	@PostMapping
	public ResponseEntity<UserDto> save(
			@RequestBody 
			@NotNull(message = "Input must not NULL") 
			@Valid final UserDto userDto) {
		log.info("*** UserDto, resource; save user *");
		return ResponseEntity.ok(this.userService.save(userDto));
	}
	
	@PutMapping
	public ResponseEntity<UserDto> update(
			@RequestBody 
			@NotNull(message = "Input must not NULL") 
			@Valid final UserDto userDto) {
		log.info("*** UserDto, resource; update user *");
		return ResponseEntity.ok(this.userService.update(userDto));
	}
	
	@PutMapping("/{userId}")
	public ResponseEntity<UserDto> update(
			@PathVariable("userId") final String userId, 
			@RequestBody 
			@NotNull(message = "Input must not NULL") 
			@Valid final UserDto userDto) {
		log.info("*** UserDto, resource; update user with userId *");
		try {
			final int id = Integer.parseInt(userId.strip());
			if (id <= 0) {
				throw new ValidationException("User ID must be a positive integer");
			}
			return ResponseEntity.ok(this.userService.update(id, userDto));
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid user ID format");
		}
	}
	
	@DeleteMapping("/{userId}")
	public ResponseEntity<Boolean> deleteById(@PathVariable("userId") final String userId) {
		log.info("*** Boolean, resource; delete user by id *");
		try {
			final int id = Integer.parseInt(userId.strip());
			if (id <= 0) {
				throw new ValidationException("User ID must be a positive integer");
			}
			this.userService.deleteById(id);
			return ResponseEntity.ok(true);
		} catch (NumberFormatException e) {
			throw new ValidationException("Invalid user ID format");
		}
	}
	
	@GetMapping("/username/{username}")
	public ResponseEntity<UserDto> findByUsername(@PathVariable("username") final String username) {
		log.info("*** UserDto, resource; fetch user by username *");
		if (username == null || username.strip().isEmpty()) {
			throw new ValidationException("Username must not be blank");
		}
		return ResponseEntity.ok(this.userService.findByUsername(username.strip()));
	}
	
	
	
}










