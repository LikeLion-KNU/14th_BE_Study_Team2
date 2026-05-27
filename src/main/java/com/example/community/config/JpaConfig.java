package com.example.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * [JPA Auditing 설정]
 *
 * @EnableJpaAuditing: BaseTimeEntity의 @CreatedDate, @LastModifiedDate가 동작하게 활성화
 * → 엔티티 저장/수정 시 createdAt, updatedAt을 자동으로 현재 시각으로 설정해줌
 *
 * 왜 CommunityApplication에서 분리했나?
 * → @WebMvcTest는 Controller 계층만 로드하는 슬라이스 테스트
 * → @SpringBootApplication + @EnableJpaAuditing 이 함께 있으면
 *   @WebMvcTest가 JPA Auditing을 초기화하려다 JPA 컨텍스트가 없어서 오류 발생
 * → 별도 @Configuration 클래스로 분리하면 @WebMvcTest가 이 클래스를 로드하지 않아 해결
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
