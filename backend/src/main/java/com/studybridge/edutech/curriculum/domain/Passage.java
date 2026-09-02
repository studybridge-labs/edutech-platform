package com.studybridge.edutech.curriculum.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * 영어 독해 지문을 나타내는 Entity입니다.
 *
 * <p>하나의 단원에 속하며, 하나의 지문에는 여러 문제가 연결될 수 있습니다.
 * 독해 문제가 아닌 문제는 지문을 가지지 않을 수 있습니다.</p>
 */
@Entity
@Table(
        name = "passages",
        indexes = {
                @Index(
                        name = "idx_passages_unit",
                        columnList = "unit_id"
                )
        }
)
public class Passage extends BaseTimeEntity {

    /**
     * 지문 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 이 지문이 속한 단원입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "unit_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_passages_unit")
    )
    private Unit unit;

    /**
     * 지문의 제목입니다.
     *
     * <p>제목이 없는 지문이 있을 수 있으므로 nullable입니다.</p>
     */
    @Column(length = 200)
    private String title;

    /**
     * 지문 본문입니다.
     *
     * <p>Markdown 및 LaTeX 표현을 포함할 수 있으며,
     * 렌더링 시 신뢰할 수 없는 입력으로 취급하여 Sanitization을 적용합니다.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 지문의 활성화 여부입니다.
     *
     * <p>비활성 지문은 학생에게 노출되지 않지만
     * 기존 학습 기록을 위해 데이터는 유지합니다.</p>
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 Passage 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected Passage() {
    }

    private Passage(
            Unit unit,
            String title,
            String content,
            boolean active
    ) {
        this.unit = unit;
        this.title = title;
        this.content = content;
        this.active = active;
    }

    /**
     * 새로운 지문을 생성합니다.
     *
     * <p>생성된 지문은 기본적으로 활성 상태로 시작합니다.
     * 제목은 없을 수 있으므로 null을 허용합니다.</p>
     *
     * @param unit    지문이 속한 단원
     * @param title   지문 제목 (없으면 null)
     * @param content 지문 본문
     * @return 생성된 Passage
     */
    public static Passage create(
            Unit unit,
            String title,
            String content
    ) {
        return new Passage(unit, title, content, true);
    }

    /**
     * 지문의 제목과 본문을 변경합니다.
     *
     * @param title   변경할 제목 (없으면 null)
     * @param content 변경할 본문
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 지문을 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 지문을 비활성화합니다.
     */
    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public Unit getUnit() {
        return unit;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public boolean isActive() {
        return active;
    }
}