package com.example.community.api.user.service;

import com.example.community.api.user.exception.AuthException;
import com.example.community.domain.user.entity.NicknameAdjective;
import com.example.community.domain.user.entity.NicknameNoun;
import com.example.community.domain.user.repository.NicknameAdjectiveRepository;
import com.example.community.domain.user.repository.NicknameNounRepository;
import com.example.community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NicknameService {

    private final NicknameAdjectiveRepository adjectiveRepository;
    private final NicknameNounRepository nounRepository;
    private final UserRepository userRepository;

    public String generateUniqueNickname() {
        List<NicknameAdjective> adjectives = adjectiveRepository.findByIsActiveTrue();
        List<NicknameNoun> nouns = nounRepository.findByIsActiveTrue();

        if (adjectives.isEmpty() || nouns.isEmpty()) {
            throw new AuthException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "닉네임 단어 데이터가 부족합니다."
            );
        }

        List<String> candidates = new ArrayList<>();

        for (NicknameAdjective adjective : adjectives) {
            for (NicknameNoun noun : nouns) {
                candidates.add(adjective.getWord() + noun.getWord());
            }
        }

        Collections.shuffle(candidates);

        for (String nickname : candidates) {
            if (!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }

        throw new AuthException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "사용 가능한 닉네임 조합이 없습니다."
        );
    }
}