package com.example.likelionbackend2team.domain.nickname;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nickname_adjectives")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameAdjective {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long adjectiveId;

    @Column(nullable = false, length = 50)
    private String word;

    private Boolean isActive;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}