package com.studybridge.edutech.identity.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.Locale;
import java.util.UUID;

/**
 * EduTech Platform의 사용자 계정을 나타내는 Entity입니다.
 *
 * <p>LOCAL 및 OAuth 사용자가 공통으로 사용하는 계정 정보를 관리하며,
 * 인증 방식에 관계없이 하나의 User를 기준으로 서비스 권한과 계정 상태를 관리합니다.</p>
 */
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class User extends BaseTimeEntity {

    /**
     * 사용자 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 로그인 및 사용자 식별에 사용하는 이메일입니다.
     *
     * <p>이메일은 소문자로 정규화하여 저장합니다.</p>
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * LOCAL 로그인 사용자의 BCrypt 암호화 비밀번호입니다.
     *
     * <p>OAuth 전용 사용자는 비밀번호가 없을 수 있으므로 nullable입니다.</p>
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /**
     * 서비스에서 표시되는 사용자 닉네임입니다.
     *
     * <p>닉네임은 중복을 허용합니다.</p>
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 사용자의 서비스 권한입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /**
     * 사용자 계정의 현재 상태입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 User 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected User() {
    }

    private User(
            String email,
            String passwordHash,
            String nickname,
            Role role,
            UserStatus status
    ) {
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
        this.status = status;
    }

    /**
     * LOCAL 회원가입 사용자를 생성합니다.
     *
     * <p>일반 회원가입 사용자는 USER 권한과 ACTIVE 상태로 시작합니다.
     * passwordHash에는 반드시 BCrypt 등으로 암호화가 완료된 값을 전달해야 합니다.</p>
     *
     * @param email        사용자 이메일
     * @param passwordHash 암호화된 비밀번호
     * @param nickname     사용자 닉네임
     * @return 생성된 User
     */
    public static User createLocal(
            String email,
            String passwordHash,
            String nickname
    ) {
        return new User(
                email,
                passwordHash,
                nickname,
                Role.USER,
                UserStatus.ACTIVE
        );
    }

    /**
     * 이메일 비교의 일관성을 위해 앞뒤 공백을 제거하고 소문자로 변환합니다.
     */
    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getNickname() {
        return nickname;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }
}