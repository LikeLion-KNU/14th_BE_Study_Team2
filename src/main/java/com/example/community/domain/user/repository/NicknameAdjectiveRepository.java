package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.NicknameAdjective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NicknameAdjectiveRepository extends JpaRepository<NicknameAdjective, Long> {

    List<NicknameAdjective> findByIsActiveTrue();
}