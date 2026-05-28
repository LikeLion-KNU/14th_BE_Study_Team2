package com.example.community.api.user.dto;

import com.example.community.domain.user.entity.User;
import com.example.community.domain.user.enums.UserStatus;
import com.example.community.domain.user.enums.UserRole;
import lombok.*;
// 이 DTO는 서버가 프론트에게 데이터를 포장해서 보내줄 때 쓰는 상자
@Getter
@Builder // 객체를 만들 때 순서에 상관없이 내가 원하는 데이터만 골라서 넣을 수 있음
// Dto.builder().nickname("혁진").build();
@NoArgsConstructor
@AllArgsConstructor
public class MyPageResponse {
    private String studentId;
    private String nickname;
    private String name;
    private String school;
    private UserStatus status;
    private UserRole role;
    //(User 엔티티를 받아서 DTO로 바로 변환해 주는 메서드)

    public static MyPageResponse from(User user) {
        return MyPageResponse.builder()
                .studentId(String.valueOf(user.getStudentId()))
                .nickname(user.getNickname())
                .name(user.getName())
                .status(user.getStatus())
                .role(user.getRole())
                .build();
    }
}