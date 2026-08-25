package com.studybridge.edutech.student.infrastructure;

import com.studybridge.edutech.student.domain.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * StudentProfile Entity의 영속성 처리를 담당하는 Repository입니다.
 */
public interface StudentProfileRepository extends JpaRepository<StudentProfile, UUID> {
}