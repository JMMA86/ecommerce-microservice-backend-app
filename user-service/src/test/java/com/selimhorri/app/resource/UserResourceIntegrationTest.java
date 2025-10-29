package com.selimhorri.app.resource;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.selimhorri.app.config.TestSecurityConfig;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.dto.response.collection.DtoCollectionResponse;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.service.UserService;

/**
 * Integration tests for UserResource.
 *
 * <p>These tests verify the REST API endpoints behavior, including request/response mapping,
 * validation, error handling, and proper HTTP status codes.
 */
@WebMvcTest(UserResource.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("UserResource - Integration Tests")
class UserResourceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private UserDto testUserDto;
    private CredentialDto testCredentialDto;
    private DtoCollectionResponse<UserDto> testCollectionResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        setupTestData();
    }

    private void setupTestData() {
        // Credential DTO
        testCredentialDto = CredentialDto.builder()
                .credentialId(1)
                .username("john")
                .password("password")
                .roleBasedAuthority(RoleBasedAuthority.ROLE_USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .build();

        // User DTO
        testUserDto = UserDto.builder()
                .userId(1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .credentialDto(testCredentialDto)
                .build();

        // Collection Response
        List<UserDto> users = Arrays.asList(testUserDto);
        testCollectionResponse = new DtoCollectionResponse<>();
        testCollectionResponse.setCollection(users);
    }

    @Nested
    @DisplayName("Get Users")
    class GetUsers {

        @Test
        @DisplayName("Should get all users successfully")
        void shouldGetAllUsersSuccessfully() throws Exception {
            // Given
            when(userService.findAll()).thenReturn(Arrays.asList(testUserDto));

            // When & Then
            mockMvc
                    .perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.collection", hasSize(1)))
                    .andExpect(jsonPath("$.collection[0].userId", is(1)))
                    .andExpect(jsonPath("$.collection[0].firstName", is("John")))
                    .andExpect(jsonPath("$.collection[0].lastName", is("Doe")))
                    .andExpect(jsonPath("$.collection[0].email", is("john@example.com")));

            verify(userService).findAll();
        }

        @Test
        @DisplayName("Should get user by ID successfully")
        void shouldGetUserByIdSuccessfully() throws Exception {
            // Given
            when(userService.findById(1)).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")));

            verify(userService).findById(1);
        }

        @Test
        @DisplayName("Should return 404 when user not found by ID")
        void shouldReturn404WhenUserNotFoundById() throws Exception {
            // Given
            when(userService.findById(999)).thenThrow(new UserObjectNotFoundException("User not found"));

            // When & Then
            mockMvc
                    .perform(get("/api/users/999"))
                    .andExpect(status().isNotFound());

            verify(userService).findById(999);
        }

        @Test
        @DisplayName("Should get user by username successfully")
        void shouldGetUserByUsernameSuccessfully() throws Exception {
            // Given
            when(userService.findByUsername("john")).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(get("/api/users/username/john"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")));

            verify(userService).findByUsername("john");
        }

        @Test
        @DisplayName("Should return 404 when user not found by username")
        void shouldReturn404WhenUserNotFoundByUsername() throws Exception {
            // Given
            when(userService.findByUsername("nonexistent")).thenThrow(new UserObjectNotFoundException("User not found"));

            // When & Then
            mockMvc
                    .perform(get("/api/users/username/nonexistent"))
                    .andExpect(status().isNotFound());

            verify(userService).findByUsername("nonexistent");
        }
    }

    @Nested
    @DisplayName("Create User")
    class CreateUser {

        @Test
        @DisplayName("Should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {
            // Given
            when(userService.save(any(UserDto.class))).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(
                            post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testUserDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")))
                    .andExpect(jsonPath("$.lastName", is("Doe")));

            verify(userService).save(any(UserDto.class));
        }

        @Test
        @DisplayName("Should create user even with empty firstName")
        void shouldCreateUserEvenWithEmptyFirstName() throws Exception {
            // Given
            UserDto invalidDto = UserDto.builder()
                    .firstName("")
                    .lastName("Test")
                    .email("test@example.com")
                    .credentialDto(testCredentialDto)
                    .build();
            when(userService.save(any(UserDto.class))).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(
                            post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed")
        void shouldReturn400WhenRequestBodyIsMalformed() throws Exception {
            // When & Then
            mockMvc
                    .perform(
                            post("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{ invalid json }"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update User")
    class UpdateUser {

        @Test
        @DisplayName("Should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {
            // Given
            when(userService.update(any(UserDto.class))).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(
                            put("/api/users")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testUserDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)))
                    .andExpect(jsonPath("$.firstName", is("John")));

            verify(userService).update(any(UserDto.class));
        }

        @Test
        @DisplayName("Should update user by ID successfully")
        void shouldUpdateUserByIdSuccessfully() throws Exception {
            // Given
            when(userService.update(eq(1), any(UserDto.class))).thenReturn(testUserDto);

            // When & Then
            mockMvc
                    .perform(
                            put("/api/users/1")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testUserDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.userId", is(1)));

            verify(userService).update(eq(1), any(UserDto.class));
        }

        @Test
        @DisplayName("Should return 404 when updating non-existent user")
        void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
            // Given
            when(userService.update(eq(999), any(UserDto.class))).thenThrow(new UserObjectNotFoundException("User not found"));

            // When & Then
            mockMvc
                    .perform(
                            put("/api/users/999")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(testUserDto)))
                    .andExpect(status().isNotFound());

            verify(userService).update(eq(999), any(UserDto.class));
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUser {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Given
            doNothing().when(userService).deleteById(1);

            // When & Then
            mockMvc
                    .perform(delete("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("true"));

            verify(userService).deleteById(1);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent user")
        void shouldReturn404WhenDeletingNonExistentUser() throws Exception {
            // Given
            doThrow(new UserObjectNotFoundException("User not found")).when(userService).deleteById(999);

            // When & Then
            mockMvc
                    .perform(delete("/api/users/999"))
                    .andExpect(status().isNotFound());

            verify(userService).deleteById(999);
        }
    }

    @Nested
    @DisplayName("Validation")
    class Validation {

        @Test
        @DisplayName("Should return 400 for blank userId in path")
        void shouldReturn400ForBlankUserIdInPath() throws Exception {
            // When & Then
            mockMvc
                    .perform(get("/api/users/ "))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle blank username in path")
        void shouldHandleBlankUsernameInPath() throws Exception {
            // Given
            when(userService.findByUsername(" ")).thenThrow(new UserObjectNotFoundException("User not found"));

            // When & Then
            mockMvc
                    .perform(get("/api/users/username/{username}", " "))
                    .andExpect(status().isBadRequest());
        }
    }
}