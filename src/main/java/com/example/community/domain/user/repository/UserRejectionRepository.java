package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.UserRejection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRejectionRepository extends JpaRepository<UserRejection, Long> {
}
