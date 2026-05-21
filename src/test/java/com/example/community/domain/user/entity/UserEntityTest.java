package com.example.community.domain.user.entity;

import com.example.community.domain.user.enums.UserStatus;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    @Test
    void create_returnsUserWithPendingStatus() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
        assertThat(user.getStudentId()).isEqualTo(20201234L);
    }

    @Test
    void approve_changesStatusToApproved() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.approve();
        assertThat(user.getStatus()).isEqualTo(UserStatus.APPROVED);
        assertThat(user.getApprovedAt()).isNotNull();
    }

    @Test
    void reject_changesStatusToRejected() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.reject();
        assertThat(user.getStatus()).isEqualTo(UserStatus.REJECTED);
    }

    @Test
    void ban_changesStatusToBanned() {
        User user = User.create(20201234L, "pw", "홍길동", "경북대학교", "닉네임1", "http://cert.url");
        user.ban();
        assertThat(user.getStatus()).isEqualTo(UserStatus.BANNED);
    }
}
