package com.promoit.shortLink.service;

import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Получение существующего пользователя по ID должно возвращать пользователя без создания нового")
    void getOrCreateUser_WithExistingUserId_ShouldReturnExistingUser() {
        String existingUserId = UUID.randomUUID().toString();
        UserEntity existingUser = new UserEntity();
        when(userRepository.findById(existingUserId)).thenReturn(Optional.of(existingUser));

        UserEntity result = userService.getOrCreateUser(existingUserId);

        assertEquals(existingUser, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение нового пользователя по ID должно создавать и сохранять пользователя")
    void getOrCreateUser_WithNewUserId_ShouldCreateNewUser() {
        String newUserId = UUID.randomUUID().toString();
        when(userRepository.findById(newUserId)).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.getOrCreateUser(newUserId);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Получение пользователя с null ID должно создавать нового пользователя с автоматическим ID")
    void getOrCreateUser_WithNullUserId_ShouldCreateNewUser() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        UserEntity result = userService.getOrCreateUser(null);

        assertNotNull(result);
        assertNotNull(result.getId());
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    @DisplayName("Поиск существующего пользователя по ID должен возвращать Optional с пользователем")
    void getUserById_WithExistingUser_ShouldReturnUser() {
        String userId = UUID.randomUUID().toString();
        UserEntity user = new UserEntity();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.getUserById(userId);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    @DisplayName("Поиск несуществующего пользователя по ID должен возвращать пустой Optional")
    void getUserById_WithNonExistentUser_ShouldReturnEmpty() {
        String userId = UUID.randomUUID().toString();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserById(userId);

        assertTrue(result.isEmpty());
    }
}