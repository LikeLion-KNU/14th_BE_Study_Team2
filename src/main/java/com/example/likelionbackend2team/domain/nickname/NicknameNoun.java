package com.example.likelionbackend2team.domain.nickname;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import lombok.*;

@Entity
@Table(name = "nickname_nouns")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NicknameNoun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nounId;

    @Column(nullable = false, length = 50)
    private String word;

    private Boolean isActive;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
