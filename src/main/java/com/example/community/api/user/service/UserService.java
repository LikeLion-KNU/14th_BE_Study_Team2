package com.example.community.api.user.service;

import com.example.community.api.user.dto.MyPageResponse;
import com.example.community.api.user.dto.UserUpdateRequest;
import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service // 스피링에게 비즈니스 로직을 처리하는거라고 알려줌
@RequiredArgsConstructor //
@Transactional(readOnly = true) // 읽기 전용, DB 내용을 바꾸지 않고 단순히 조회만 할 때 켜두면 DB의 성능이 빨라짐
public class UserService {

    private final UserRepository userRepository;

    // [기능 1, 2] 로그인한 사용자 정보 및 마이페이지 조회
    public MyPageResponse getMyPage(Long userId) {
        User user = findUserById(userId);
        return MyPageResponse.from(user); // DTO에서 필드 관리 (studentId, nickname 등)
    }

    // [기능 4] 닉네임 수정 시 중복 체크
    public void validateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }
    }

    // [기능 5, 6] 회원 정보 수정 (본인 확인 포함)
    @Transactional // 읽기 + 쓰기
    public void updateUserInfo(Long userId, UserUpdateRequest requestDto) {
        User user = findUserById(userId);

        // 닉네임이 변경된 경우에만 중복 체크 실행
        if (requestDto.getNickname() != null && !user.getNickname().equals(requestDto.getNickname())) {
            validateNickname(requestDto.getNickname()); // 중복체크 로직
            user.updateNickname(requestDto.getNickname()); // 닉네임 더티 체킹
        }

        // 이름 변경 요청
        if (requestDto.getName() != null && !user.getName().equals(requestDto.getName())){
            user.updateName(requestDto.getName());
        }
        // 비밀번호는 AuthService, Password로 뺌
    }

    // [기능 5, 6] 회원 삭제/탈퇴 (본인 확인 포함)
    @Transactional
    public void deleteUser(Long userId) {
        User user = findUserById(userId);
        userRepository.delete(user);
    }

    // 공통 로직: 유저 찾기 및 예외 처리
    // 마이페이지 조회, 정보 수정, 회원 탈퇴를 할 때 공통적으로 ID로 유저를 찾고,
    // 없으면 에러 던지기 작업
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. ID: " + userId));
    }
}