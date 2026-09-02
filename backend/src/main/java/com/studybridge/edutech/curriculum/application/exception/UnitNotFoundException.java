package com.studybridge.edutech.curriculum.application.exception;

import java.util.UUID;

/**
 * 존재하지 않는 지문을 조회하려 할 때 발생합니다.
 */
public class UnitNotFoundException extends RuntimeException {

    public UnitNotFoundException() {
        super("지문을 찾을 수 없습니다.");
    }

    public UnitNotFoundException(UUID passageId) {
        super("지문을 찾을 수 없습니다. passageId=" + passageId);
    }
}