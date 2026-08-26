package com.studybridge.edutech.identity.infrastructure;

import com.studybridge.edutech.identity.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * User Entity의 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>회원가입 시 이메일 중복 여부를 확인하고,
 * 사용자 계정을 저장하거나 조회할 때 사용합니다.</p>
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * 회원가입 시 이메일 중복 여부를 확인합니다.
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * 로그인 시 이메일을 기준으로 사용자를 조회합니다.
     */
    Optional<User> findByEmailIgnoreCase(String email);
}