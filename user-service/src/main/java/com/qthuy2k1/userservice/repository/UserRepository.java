package com.qthuy2k1.userservice.repository;

import com.qthuy2k1.userservice.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    //    @Query("SELECT COUNT(*) > 0 FROM User u WHERE u.email = :email")
    boolean existsByEmail(String email);

    Optional<UserModel> findByEmail(String email);
}
