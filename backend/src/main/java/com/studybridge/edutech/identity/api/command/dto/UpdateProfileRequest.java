package com.studybridge.edutech.identity.api.command.dto;

import com.studybridge.edutech.student.domain.Grade;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 프로필 변경 요청 DTO입니다. (PATCH /api/v1/me/profile)
 *
 * <p>PATCH는 "바꿀 항목만 보내는" 방식이므로 모든 필드가 선택입니다.
 * 필드를 보내지 않거나 null이면 해당 항목은 변경하지 않습니다.</p>
 *
 * <p>단, 값을 보냈다면 회원가입과 같은 규칙으로 검증합니다.</p>
 *
 * @param nickname 변경할 닉네임 (선택, 2~50자, 공백만으로는 불가)
 * @param grade    변경할 학년 (선택)
 */
public record UpdateProfileRequest(

        // @NotBlank를 쓰지 않는 이유: @NotBlank는 null도 거부하므로 닉네임이 "필수"가 되어버린다.
        // @Size, @Pattern은 null을 "검사 대상 아님(통과)"으로 처리하므로 선택 필드에 맞다.
        @Size(
                min = 2,
                max = 50,
                message = "닉네임은 2자 이상 50자 이하로 입력해주세요."
        )
        // 공백이 아닌 글자가 최소 1개는 있어야 한다. ("   " 같은 공백만 있는 닉네임 차단)
        @Pattern(
                regexp = ".*\\S.*",
                message = "닉네임은 공백만으로 입력할 수 없습니다."
        )
        String nickname,

        // 검증 어노테이션 없음: null이면 변경 안 함,
        // 값이 있으면 Jackson이 "MIDDLE_3" 같은 문자열을 Grade enum으로 변환한다.
        Grade grade
) {
}