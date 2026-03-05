package sopt.comfit.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EJobStatus {
    PENDING("수정"),
    PROCESSING("작업중"),
    COMPLETED("완료"),
    FAILED("실패");

    private final String description;
}
