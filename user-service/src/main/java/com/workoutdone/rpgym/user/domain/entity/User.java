package com.workoutdone.rpgym.user.domain.entity;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedDeletedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", schema = "user_service")
public class User extends BaseCreatedUpdatedDeletedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "slack_id", nullable = false, length = 100)
    private String slackId;

    private User(String email, String password, String nickname, String slackId) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.slackId = slackId;
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
    }

    // password는 이미 해시된 값을 받는다. 평문 해싱은 application 계층(PasswordEncoder)의 책임이다.
    public static User create(String email, String encodedPassword, String nickname, String slackId) {
        return new User(email, encodedPassword, nickname, slackId);
    }
}
