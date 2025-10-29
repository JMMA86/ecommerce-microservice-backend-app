package com.selimhorri.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.selimhorri.app.domain.Credential;
import com.selimhorri.app.domain.RoleBasedAuthority;
import com.selimhorri.app.domain.User;
import com.selimhorri.app.dto.CredentialDto;
import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.exception.wrapper.UserObjectNotFoundException;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private Credential credential;
    private CredentialDto credentialDto;

    @BeforeEach
    void setUp() {
        credential = new Credential();
        credential.setCredentialId(1);
        credential.setUsername("john");
        credential.setPassword("password");
        credential.setRoleBasedAuthority(RoleBasedAuthority.ROLE_USER);
        credential.setIsEnabled(true);
        credential.setIsAccountNonExpired(true);
        credential.setIsAccountNonLocked(true);
        credential.setIsCredentialsNonExpired(true);

        user = new User();
        user.setUserId(1);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCredential(credential);

        credentialDto = new CredentialDto();
        credentialDto.setCredentialId(1);
        credentialDto.setUsername("john");
        credentialDto.setPassword("password");
        credentialDto.setRoleBasedAuthority(RoleBasedAuthority.ROLE_USER);
        credentialDto.setIsEnabled(true);
        credentialDto.setIsAccountNonExpired(true);
        credentialDto.setIsAccountNonLocked(true);
        credentialDto.setIsCredentialsNonExpired(true);

        userDto = new UserDto();
        userDto.setUserId(1);
        userDto.setFirstName("John");
        userDto.setLastName("Doe");
        userDto.setCredentialDto(credentialDto);
    }

    @Test
    void testFindAll() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<UserDto> result = userService.findAll();
        assertEquals(1, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        UserDto result = userService.findById(1);
        assertEquals(userDto.getUserId(), result.getUserId());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void testFindByIdNotFound() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(UserObjectNotFoundException.class, () -> userService.findById(1));
    }

    @Test
    void testSave() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto result = userService.save(userDto);
        assertEquals(userDto.getUserId(), result.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdate() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto result = userService.update(userDto);
        assertEquals(userDto.getUserId(), result.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateWithId() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        UserDto result = userService.update(1, userDto);
        assertEquals(userDto.getUserId(), result.getUserId());
        verify(userRepository, times(1)).findById(1);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteById() {
        userService.deleteById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByCredentialUsername("john")).thenReturn(Optional.of(user));
        UserDto result = userService.findByUsername("john");
        assertEquals(userDto.getUserId(), result.getUserId());
        verify(userRepository, times(1)).findByCredentialUsername("john");
    }

    @Test
    void testFindByUsernameNotFound() {
        when(userRepository.findByCredentialUsername("john")).thenReturn(Optional.empty());
        assertThrows(UserObjectNotFoundException.class, () -> userService.findByUsername("john"));
    }

}