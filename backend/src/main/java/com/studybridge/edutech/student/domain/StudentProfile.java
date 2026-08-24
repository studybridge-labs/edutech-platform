package com.studybridge.edutech.student.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import com.studybridge.edutech.identity.domain.User;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * 학생 사용자의 학습 프로필을 나타내는 Entity입니다.
 *
 * <p>User와 1:1 관계를 가지며,
 * 현재 학년과 같이 학습 콘텐츠 제공에 필요한 학생 정보를 관리합니다.</p>
 */
@Entity
@Table(
        name = "student_profiles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_student_profiles_user_id",
                        columnNames = "user_id"
                )
        }
)
public class StudentProfile extends BaseTimeEntity {

    /**
     * 학생 프로필 식별자입니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 이 학생 프로필이 속한 사용자입니다.
     *
     * <p>한 User는 하나의 StudentProfile만 가질 수 있습니다.</p>
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_student_profiles_user")
    )
    private User user;

    /**
     * 학생의 현재 학년입니다.
     *
     * <p>진단평가 출제, 단원 조회, 추천 문제 제공 등의 기준으로 사용됩니다.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Grade grade;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     */
    protected StudentProfile() {
    }

    private StudentProfile(User user, Grade grade) {
        this.user = user;
        this.grade = grade;
    }

    /**
     * 새로운 학생 프로필을 생성합니다.
     *
     * @param user  프로필의 소유 사용자
     * @param grade 학생의 현재 학년
     * @return 생성된 StudentProfile
     */
    public static StudentProfile create(User user, Grade grade) {
        return new StudentProfile(user, grade);
    }

    /**
     * 학생의 현재 학년을 변경합니다.
     *
     * @param grade 변경할 학년
     */
    public void changeGrade(Grade grade) {
        this.grade = grade;
    }

    public UUID getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Grade getGrade() {
        return grade;
    }
}