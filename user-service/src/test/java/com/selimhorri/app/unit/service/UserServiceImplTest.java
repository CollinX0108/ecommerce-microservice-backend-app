package com.selimhorri.app.unit.service;

import com.selimhorri.app.dto.UserDto;
import com.selimhorri.app.repository.UserRepository;
import com.selimhorri.app.service.impl.UserServiceImpl;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.selimhorri.app.unit.util.UserUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserUtil.getSampleUserDto();
    }

    @Test
    void testFindById_ShouldReturnUserDto() {
        when(userRepository.findById(userDto.getUserId())).thenReturn(Optional.of(UserUtil.getSampleUser()));

        UserDto result = userService.findById(userDto.getUserId());

        assertNotNull(result);
        assertEquals(userDto.getUserId(), result.getUserId());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getLastName(), result.getLastName());

    }

    @Test
    void testSave_ShouldReturnUserDto() {
        when(userRepository.save(any())).thenReturn(UserUtil.getSampleUser());

        UserDto result = userService.save(userDto);

        assertNotNull(result);
        assertEquals(userDto.getUserId(), result.getUserId());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        assertEquals(userDto.getLastName(), result.getLastName());
    }

    @Test
    void testFindById_ShouldThrowException_WhenUserNotFound() {
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.findById(999));
        verify(userRepository).findById(999);
    }

    @Test
    void testUpdate_ShouldReturnUpdatedUserDto() {
        when(userRepository.save(any())).thenReturn(UserUtil.getSampleUser());

        UserDto result = userService.update(userDto);

        assertNotNull(result);
        assertEquals(userDto.getUserId(), result.getUserId());
        assertEquals(userDto.getFirstName(), result.getFirstName());
        verify(userRepository).save(any());
    }

    @Test
    void testDeleteById_ShouldDeleteUser() {
        userService.deleteById(userDto.getUserId());

        verify(userRepository).deleteById(userDto.getUserId());
    }

    @Test
    void testFindAll_ShouldReturnUserList() {
        when(userRepository.findAll()).thenReturn(UserUtil.getSampleUsers());

        var result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }


}