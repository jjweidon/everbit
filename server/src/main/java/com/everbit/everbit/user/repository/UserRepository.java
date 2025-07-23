package com.everbit.everbit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.everbit.everbit.user.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    List<User> findByIsBotActiveTrue();
}
