package com.example.community.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "nickname_nouns") // 명사는 nickname_nouns
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class NicknameNoun { // 명사는 NicknameNoun

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "noun_id") // 명사는 noun_id
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String word;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static NicknameNoun create(String word) {
        NicknameNoun noun = new NicknameNoun();
        noun.word = word;
        noun.isActive = true;
        return noun;
    }
}