package com.promoit.shortLink.domain.repository;


import com.promoit.shortLink.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findById(String id);
}
