package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_Id(Long userId);
}
