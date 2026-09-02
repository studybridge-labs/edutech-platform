package com.studybridge.edutech.curriculum.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * 객관식 문제의 선택지를 나타내는 Entity입니다.
 *
 * <p>하나의 문제에 속하며, 문제 내에서 선택지 번호로 구분됩니다.
 * 정답 여부인 {@code isCorrect}는 학생용 조회 응답에 절대 포함하지 않습니다.</p>
 */
@Entity
@Table(
        name = "question_options",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_question_options_question_no",
                        columnNames = {"question_id", "option_no"}
                )
        }
)
public class QuestionOption extends BaseTimeEntity {

    /**
     * 선택지 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 이 선택지가 속한 문제입니다.
     *
     * <p>연관관계의 주인으로서 외래 키를 관리합니다.
     * 선택지 생성은 {@link Question#addOption(int, String, boolean)}을 통해 수행합니다.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "question_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_options_question")
    )
    private Question question;

    /**
     * 문제 내에서의 선택지 번호입니다.
     *
     * <p>같은 문제 안에서 중복되지 않으며, 선택지를 순서대로 노출하는 데 사용됩니다.</p>
     */
    @Column(name = "option_no", nullable = false)
    private int optionNo;

    /**
     * 선택지 내용입니다.
     *
     * <p>Markdown 및 LaTeX 표현을 포함할 수 있습니다.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 이 선택지가 정답인지 여부입니다.
     *
     * <p>정답 정보이므로 학생용 조회 응답에는 절대 포함하지 않으며,
     * Backend 채점 과정에서만 사용합니다.</p>
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 QuestionOption 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected QuestionOption() {
    }

    private QuestionOption(
            Question question,
            int optionNo,
            String content,
            boolean isCorrect
    ) {
        this.question = question;
        this.optionNo = optionNo;
        this.content = content;
        this.isCorrect = isCorrect;
    }

    /**
     * 새로운 선택지를 생성합니다.
     *
     * <p>일반적으로 {@link Question#addOption(int, String, boolean)}을 통해
     * 호출되며, Question과의 연관관계를 함께 설정합니다.</p>
     *
     * @param question  선택지가 속한 문제
     * @param optionNo  선택지 번호
     * @param content   선택지 내용
     * @param isCorrect 정답 여부
     * @return 생성된 QuestionOption
     */
    static QuestionOption create(
            Question question,
            int optionNo,
            String content,
            boolean isCorrect
    ) {
        return new QuestionOption(question, optionNo, content, isCorrect);
    }

    public UUID getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public int getOptionNo() {
        return optionNo;
    }

    public String getContent() {
        return content;
    }

    public boolean isCorrect() {
        return isCorrect;
    }
}