package com.workoutdone.rpgym.user.domain.entity;

import com.workoutdone.rpgym.common.entity.BaseCreatedUpdatedDeletedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users", schema = "user_service")
public class User extends BaseCreatedUpdatedDeletedEntity implements Persistable<UUID> {

    @Id
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

    // UUID를 애플리케이션에서 직접 생성해 미리 채워두기 때문에, JPA 기본 규칙(id가 null이면 new)으로는
    // save() 호출 시 "이미 존재하는 엔티티"로 오인해 persist() 대신 merge()가 호출된다.
    // 이 경우 @CreatedDate 등 auditing 콜백이 원본이 아닌 merge()가 반환한 별도 인스턴스에만 채워져
    // 호출부에서 들고 있는 엔티티에는 반영되지 않는 문제가 생긴다. Persistable로 새 엔티티 여부를
    // 직접 판단하게 하여 항상 persist()가 호출되도록 한다.
    @Transient
    private boolean isNew = true;

    private User(UUID id, String email, String password, String nickname, String slackId) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.slackId = slackId;
        this.role = UserRole.USER;
        this.status = UserStatus.ACTIVE;
    }

    // password는 이미 해시된 값을 받는다. 평문 해싱은 application 계층(PasswordEncoder)의 책임이다.
    public static User create(String email, String encodedPassword, String nickname, String slackId) {
        return new User(UUID.randomUUID(), email, encodedPassword, nickname, slackId);
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @PostPersist
    @PostLoad
    void markNotNew() {
        this.isNew = false;
    }
}
