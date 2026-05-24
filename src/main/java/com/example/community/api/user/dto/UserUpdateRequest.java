package com.example.community.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
// 이 DTO는 프론트엔드가 보낸 JSON 데이터를 서버가 받을 때 쓰는 상자
// 어노테이션이라고 부르고 Lombok이라는 라이브러리 기능
@Getter // 데이터 꺼내기
@NoArgsConstructor // 빈 상자 만들기
@AllArgsConstructor // 모든 변수를 다 집어넣어야만 상자가 만들어지는 생성자를 만듬
public class UserUpdateRequest {

    private String nickname; // 수정할 새로운 닉네임
    private String name;

}
// builder가 없음 , 클라이언트가 데이터를 보내면 스프링 부트가 알아서 조립해줌
// 스프링의 작업 방식:
// 1. NoArgsConstructor를 이용해 빈 상자를 만듬
// 2. 프론트엔드가 보낸 JSON을 읽고, 상자 안에 알맞은 자리를 찾아 데이터를 넣어줌
// 3. 완성된 상자를 UserController에 던져줌