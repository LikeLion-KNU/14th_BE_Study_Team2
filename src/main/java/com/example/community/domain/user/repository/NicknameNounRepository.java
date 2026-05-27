package com.example.community.domain.user.repository;

import com.example.community.domain.user.entity.NicknameNoun;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NicknameNounRepository extends JpaRepository<NicknameNoun, Long> {

    List<NicknameNoun> findByIsActiveTrue();
}