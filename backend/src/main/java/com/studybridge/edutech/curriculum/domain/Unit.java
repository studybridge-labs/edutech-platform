package com.studybridge.edutech.curriculum.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import com.studybridge.edutech.student.domain.Grade;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * 학습 단원을 나타내는 Entity입니다.
 *
 * <p>하나의 과목에 속하며, 자기 자신을 참조하는 계층 구조를 통해
 * 대단원과 소단원을 표현할 수 있습니다.</p>
 *
 * <p>예를 들어 "수학 - 중2 - 연립방정식"에서 "연립방정식"은
 * 상위 단원을 부모로 가지는 하위 단원이 될 수 있습니다.</p>
 */
@Entity
@Table(
        name = "units",
        indexes = {
                @Index(
                        name = "idx_units_subject_grade",
                        columnList = "subject_id, grade"
                ),
                @Index(
                        name = "idx_units_parent",
                        columnList = "parent_unit_id"
                )
        }
)
public class Unit extends BaseTimeEntity {

    /**
     * 단원 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 이 단원이 속한 과목입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "subject_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_units_subject")
    )
    private Subject subject;

    /**
     * 상위 단원입니다.
     *
     * <p>대단원처럼 최상위 단원인 경우 부모가 없으므로 nullable입니다.
     * 하위 단원은 자신이 속한 상위 단원을 부모로 참조합니다.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_unit_id",
            foreignKey = @ForeignKey(name = "fk_units_parent")
    )
    private Unit parentUnit;

    /**
     * 이 단원이 대상으로 하는 학년입니다.
     *
     * <p>학년별로 단원을 조회하거나 진단평가 문제를 출제할 때 기준으로 사용됩니다.</p>
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Grade grade;

    /**
     * 단원 이름입니다.
     *
     * <p>예: "연립방정식", "일차함수"</p>
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 같은 계층 내에서의 정렬 순서입니다.
     *
     * <p>단원 목록을 노출할 때 학습 순서에 맞게 정렬하는 데 사용됩니다.</p>
     */
    @Column(name = "order_no", nullable = false)
    private int orderNo;

    /**
     * 단원의 활성화 여부입니다.
     *
     * <p>비활성 단원은 학생에게 노출되지 않지만
     * 기존 학습 기록을 위해 데이터는 유지합니다.</p>
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 Unit 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected Unit() {
    }

    private Unit(
            Subject subject,
            Unit parentUnit,
            Grade grade,
            String name,
            int orderNo,
            boolean active
    ) {
        this.subject = subject;
        this.parentUnit = parentUnit;
        this.grade = grade;
        this.name = name;
        this.orderNo = orderNo;
        this.active = active;
    }

    /**
     * 상위 단원이 없는 최상위 단원을 생성합니다.
     *
     * <p>생성된 단원은 기본적으로 활성 상태로 시작합니다.</p>
     *
     * @param subject 단원이 속한 과목
     * @param grade   대상 학년
     * @param name    단원 이름
     * @param orderNo 정렬 순서
     * @return 생성된 Unit
     */
    public static Unit createRoot(
            Subject subject,
            Grade grade,
            String name,
            int orderNo
    ) {
        return new Unit(subject, null, grade, name, orderNo, true);
    }

    /**
     * 상위 단원에 속하는 하위 단원을 생성합니다.
     *
     * <p>하위 단원은 부모 단원과 동일한 과목에 속합니다.
     * 학년은 부모와 다를 수 있으므로 별도로 전달받습니다.</p>
     *
     * @param parentUnit 상위 단원
     * @param grade      대상 학년
     * @param name       단원 이름
     * @param orderNo    정렬 순서
     * @return 생성된 Unit
     */
    public static Unit createChild(
            Unit parentUnit,
            Grade grade,
            String name,
            int orderNo
    ) {
        return new Unit(
                parentUnit.getSubject(),
                parentUnit,
                grade,
                name,
                orderNo,
                true
        );
    }

    /**
     * 최상위 단원인지 여부를 반환합니다.
     *
     * @return 부모 단원이 없으면 true
     */
    public boolean isRoot() {
        return parentUnit == null;
    }

    /**
     * 단원 이름과 정렬 순서를 변경합니다.
     *
     * @param name    변경할 단원 이름
     * @param orderNo 변경할 정렬 순서
     */
    public void update(String name, int orderNo) {
        this.name = name;
        this.orderNo = orderNo;
    }

    /**
     * 단원을 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 단원을 비활성화합니다.
     */
    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public Subject getSubject() {
        return subject;
    }

    public Unit getParentUnit() {
        return parentUnit;
    }

    public Grade getGrade() {
        return grade;
    }

    public String getName() {
        return name;
    }

    public int getOrderNo() {
        return orderNo;
    }

    public boolean isActive() {
        return active;
    }
}