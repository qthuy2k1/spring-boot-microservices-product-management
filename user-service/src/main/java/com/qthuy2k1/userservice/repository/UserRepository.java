package com.qthuy2k1.userservice.repository;

import com.qthuy2k1.userservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Integer> {
    boolean existsByEmail(String email);

    Optional<UserModel> findByEmail(String email);
}
