package com.studybridge.edutech.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Spring Data JPA Auditing 기능을 활성화합니다.
 *
 * <p>BaseTimeEntity의 createdAt, updatedAt 값을
 * Entity 생성 및 수정 시점에 자동으로 기록합니다.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}