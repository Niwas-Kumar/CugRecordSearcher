package com.irctc.cugDirectory.repository;

import com.irctc.cugDirectory.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByRole(String role);
    List<AppUser> findAllByOrderByCreatedAtDesc();
}