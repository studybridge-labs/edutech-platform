package com.studybridge.edutech.curriculum.domain;

import com.studybridge.edutech.global.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * 학습 문제를 나타내는 Entity입니다.
 *
 * <p>객관식과 단답형을 모두 표현하며, 객관식 문제는 여러 선택지를 함께 관리하는
 * Aggregate Root 역할을 합니다.</p>
 *
 * <p>정답 정보인 정답 선택지, 단답형 정답, 해설은 학생용 조회 응답에
 * 절대 포함되어서는 안 되며, 정답 판정은 Backend에서만 수행합니다.</p>
 */
@Entity
@Table(
        name = "questions",
        indexes = {
                @Index(
                        name = "idx_questions_unit",
                        columnList = "unit_id"
                ),
                @Index(
                        name = "idx_questions_passage",
                        columnList = "passage_id"
                ),
                @Index(
                        name = "idx_questions_unit_difficulty",
                        columnList = "unit_id, difficulty"
                ),
                @Index(
                        name = "idx_questions_unit_type",
                        columnList = "unit_id, question_type"
                )
        }
)
public class Question extends BaseTimeEntity {

    /**
     * 문제 식별자입니다.
     *
     * <p>외부에 노출될 수 있는 Resource 식별자로 UUID를 사용합니다.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * 이 문제가 속한 단원입니다.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "unit_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_questions_unit")
    )
    private Unit unit;

    /**
     * 이 문제가 연결된 독해 지문입니다.
     *
     * <p>독해 문제가 아닌 경우 지문을 가지지 않으므로 nullable입니다.</p>
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "passage_id",
            foreignKey = @ForeignKey(name = "fk_questions_passage")
    )
    private Passage passage;

    /**
     * 문제의 출제 형태입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 30)
    private QuestionType questionType;

    /**
     * 문제의 난이도입니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    /**
     * 문제 본문입니다.
     *
     * <p>Markdown 및 LaTeX 표현을 포함할 수 있습니다.</p>
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 단답형 문제의 정답입니다.
     *
     * <p>객관식 문제는 선택지로 정답을 관리하므로 이 값이 없을 수 있어 nullable입니다.
     * 학생용 조회 응답에는 절대 포함하지 않습니다.</p>
     */
    @Column(name = "correct_answer_text", columnDefinition = "TEXT")
    private String correctAnswerText;

    /**
     * 문제 해설입니다.
     *
     * <p>진단평가 진행 중이거나 학생용 문제 조회 시에는 노출하지 않습니다.</p>
     */
    @Column(columnDefinition = "TEXT")
    private String explanation;

    /**
     * 문제의 활성화 여부입니다.
     *
     * <p>오류가 발견된 문제는 비활성화하며, 기존 학습 기록을 위해 데이터는 유지합니다.</p>
     */
    @Column(nullable = false)
    private boolean active;

    /**
     * 객관식 문제의 선택지 목록입니다.
     *
     * <p>Question이 선택지의 Aggregate Root이므로, 문제 저장 시 선택지도 함께
     * 저장되고 문제 삭제 시 선택지도 함께 제거되도록 관리합니다.</p>
     */
    @OneToMany(
            mappedBy = "question",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<QuestionOption> options = new ArrayList<>();

    /**
     * JPA에서 사용하는 기본 생성자입니다.
     *
     * <p>외부에서 직접 빈 Question 객체를 생성하지 못하도록 protected로 제한합니다.</p>
     */
    protected Question() {
    }

    private Question(
            Unit unit,
            Passage passage,
            QuestionType questionType,
            Difficulty difficulty,
            String content,
            String correctAnswerText,
            String explanation,
            boolean active
    ) {
        this.unit = unit;
        this.passage = passage;
        this.questionType = questionType;
        this.difficulty = difficulty;
        this.content = content;
        this.correctAnswerText = correctAnswerText;
        this.explanation = explanation;
        this.active = active;
    }

    /**
     * 객관식 문제를 생성합니다.
     *
     * <p>선택지는 생성 후 {@link #addOption(int, String, boolean)}으로 추가합니다.
     * 객관식은 선택지로 정답을 관리하므로 정답 문자열을 받지 않습니다.</p>
     *
     * @param unit        문제가 속한 단원
     * @param passage     연결된 지문 (없으면 null)
     * @param difficulty  난이도
     * @param content     문제 본문
     * @param explanation 해설
     * @return 생성된 Question
     */
    public static Question createMultipleChoice(
            Unit unit,
            Passage passage,
            Difficulty difficulty,
            String content,
            String explanation
    ) {
        return new Question(
                unit,
                passage,
                QuestionType.MULTIPLE_CHOICE,
                difficulty,
                content,
                null,
                explanation,
                true
        );
    }

    /**
     * 단답형 문제를 생성합니다.
     *
     * <p>단답형은 선택지 없이 정답 문자열로 채점합니다.</p>
     *
     * @param unit              문제가 속한 단원
     * @param passage           연결된 지문 (없으면 null)
     * @param difficulty        난이도
     * @param content           문제 본문
     * @param correctAnswerText 정답 문자열
     * @param explanation       해설
     * @return 생성된 Question
     */
    public static Question createShortAnswer(
            Unit unit,
            Passage passage,
            Difficulty difficulty,
            String content,
            String correctAnswerText,
            String explanation
    ) {
        return new Question(
                unit,
                passage,
                QuestionType.SHORT_ANSWER,
                difficulty,
                content,
                correctAnswerText,
                explanation,
                true
        );
    }

    /**
     * 객관식 문제에 선택지를 추가합니다.
     *
     * <p>양방향 연관관계를 안전하게 설정하기 위해 이 메서드를 통해서만
     * 선택지를 추가합니다.</p>
     *
     * @param optionNo  선택지 번호
     * @param content   선택지 내용
     * @param isCorrect 정답 여부
     * @return 추가된 QuestionOption
     */
    public QuestionOption addOption(
            int optionNo,
            String content,
            boolean isCorrect
    ) {
        QuestionOption option = QuestionOption.create(
                this,
                optionNo,
                content,
                isCorrect
        );
        this.options.add(option);
        return option;
    }

    /**
     * 주어진 선택지가 이 문제의 정답인지 판정합니다.
     *
     * <p>객관식 문제의 채점에 사용합니다. 전달된 선택지 식별자가
     * 이 문제의 정답 선택지와 일치하면 정답으로 판정합니다.</p>
     *
     * @param selectedOptionId 학생이 선택한 선택지 식별자
     * @return 정답이면 true
     */
    public boolean isCorrectOption(UUID selectedOptionId) {
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .anyMatch(option -> option.getId().equals(selectedOptionId));
    }

    /**
     * 주어진 답안 문자열이 단답형 정답과 일치하는지 판정합니다.
     *
     * <p>앞뒤 공백을 제거하고 대소문자를 구분하지 않고 비교합니다.</p>
     *
     * @param answerText 학생이 입력한 답안
     * @return 정답이면 true
     */
    public boolean isCorrectAnswer(String answerText) {
        if (correctAnswerText == null || answerText == null) {
            return false;
        }
        return correctAnswerText.trim().equalsIgnoreCase(answerText.trim());
    }

    /**
     * 정답 선택지를 조회합니다.
     *
     * <p>객관식 문제에서 정답 선택지가 존재하면 반환합니다.</p>
     *
     * @return 정답 선택지 (없으면 빈 Optional)
     */
    public Optional<QuestionOption> findCorrectOption() {
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .findFirst();
    }

    /**
     * 객관식 문제인지 여부를 반환합니다.
     */
    public boolean isMultipleChoice() {
        return questionType == QuestionType.MULTIPLE_CHOICE;
    }

    /**
     * 문제 해설과 내용을 변경합니다.
     *
     * <p>정답이나 선택지 변경 제한 정책은 Application 계층에서
     * 학습 기록 존재 여부를 확인한 뒤 처리합니다.</p>
     *
     * @param content     변경할 문제 본문
     * @param explanation 변경할 해설
     */
    public void updateContent(String content, String explanation) {
        this.content = content;
        this.explanation = explanation;
    }

    /**
     * 문제를 활성화합니다.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * 문제를 비활성화합니다.
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

    public Passage getPassage() {
        return passage;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public String getContent() {
        return content;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public String getExplanation() {
        return explanation;
    }

    public boolean isActive() {
        return active;
    }

    /**
     * 선택지 목록을 반환합니다.
     *
     * <p>외부에서 컬렉션을 직접 수정하지 못하도록 방어적으로 복사하여 반환합니다.</p>
     */
    public List<QuestionOption> getOptions() {
        return List.copyOf(options);
    }
}