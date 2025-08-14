package com.setupshowroom.user;

import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, String> {
  @Query("from User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
  Optional<User> findByUsernameOrEmail(@NotNull String usernameOrEmail);

  Optional<User> findUserById(@NotNull String id);

  boolean existsByUsernameAndLockedFalseAndEnabledTrue(@NotNull String username);

  boolean existsByEmailAndLockedFalseAndEnabledTrue(@NotNull String email);
}
