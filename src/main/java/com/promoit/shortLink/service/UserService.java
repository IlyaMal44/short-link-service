package com.promoit.shortLink.service;


import com.promoit.shortLink.domain.entity.UserEntity;
import com.promoit.shortLink.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Получает существующего пользователя по ID или создает нового анонимного пользователя.
     * @param userId идентификатор пользователя из заголовка X-User-ID (может быть null)
     * @return существующий пользователь или новый анонимный пользователь
     */
    public UserEntity getOrCreateUser(String userId) {
        if (userId != null) {
            Optional<UserEntity> existingUser = userRepository.findById(userId);
            if (existingUser.isPresent()) {
                return existingUser.get();
            }
        }
        UserEntity newUser = new UserEntity();
        return userRepository.save(newUser);
    }

    /**
     * Находит пользователя по идентификатору.
     * @param userId идентификатор пользователя для поиска
     * @return Optional с найденным пользователем или empty если пользователь не существует
     */
    public Optional<UserEntity> getUserById(String userId) {
        return userRepository.findById(userId);
    }
}