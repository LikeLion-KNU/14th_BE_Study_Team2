package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByStudentId(Long studentId);

    boolean existsByStudentId(Long studentId);

    boolean existsByNickname(String nickname);
}