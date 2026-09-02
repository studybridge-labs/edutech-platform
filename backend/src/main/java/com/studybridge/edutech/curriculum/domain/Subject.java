package com.studybridge.edutech.curriculum.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * 학습 과목을 나타내는 Entity입니다.
 *
 * <p>V1에서는 MATH, ENGLISH 두 과목을 제공하며,
 * 단원과 문제 등 모든 학습 콘텐츠의 최상위 분류 기준이 됩니다.</p>
 */
@Entity
@Table(
        name = "subjects",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_subjects_code",
                        columnNames = "code"
                )
        }
)
public class Subject extends BaseTimeEntity {

    /**
     * 과목 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 과목을 구분하는 코드입니다.
     *
     * <p>MATH, ENGLISH와 같이 시스템 내부에서 과목을 식별하는 데 사용하며,
     * 중복되지 않습니다.</p>
     */
    @Column(nullable = false, length = 30)
    private String code;

    /**
     * 사용자에게 표시되는 과목 이름입니다.
     *
     * <p>예: "수학", "영어"</p>
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 과목의 활성화 여부입니다.
     *
     * <p>비활성 과목은 학생에게 노출되지 않지만
     * 기존 학습 기록을 위해 데이터는 유지합니다.</p>
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 Subject 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected Subject() {
    }

    private Subject(String code, String name, boolean active) {
        this.code = code;
        this.name = name;
        this.active = active;
    }

    /**
     * 새로운 과목을 생성합니다.
     *
     * <p>생성된 과목은 기본적으로 활성 상태로 시작합니다.</p>
     *
     * @param code 과목 코드 (예: MATH)
     * @param name 과목 이름 (예: 수학)
     * @return 생성된 Subject
     */
    public static Subject create(String code, String name) {
        return new Subject(code, name, true);
    }

    /**
     * 과목을 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 과목을 비활성화합니다.
     */
    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}