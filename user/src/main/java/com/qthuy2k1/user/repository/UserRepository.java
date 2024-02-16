package com.qthuy2k1.user.repository;

import com.qthuy2k1.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserModel, String> {
        //    @Query("SELECT COUNT(*) > 0 FROM User u WHERE u.email = :email")
        boolean existsByEmail(String email);
}
