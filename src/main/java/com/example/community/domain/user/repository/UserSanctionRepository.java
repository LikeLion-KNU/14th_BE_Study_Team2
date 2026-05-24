package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserSanction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSanctionRepository extends JpaRepository<UserSanction, Long> {
}
