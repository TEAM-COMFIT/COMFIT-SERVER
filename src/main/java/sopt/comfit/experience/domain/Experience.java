package sopt.comfit.experience.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import sopt.comfit.experience.exception.ExperienceErrorCode;
import sopt.comfit.global.base.BaseTimeEntity;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.User;

import java.time.LocalDate;

@Slf4j
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "experiences")
public class Experience extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "situation", nullable = false)
    private String situation;

    @Column(name = "task", nullable = false)
    private String task;

    @Column(name = "action", nullable = false, columnDefinition = "TEXT")
    private String action;

    @Column(name = "result", nullable = false, columnDefinition = "TEXT")
    private String result;

    @Column(name = "start_at", nullable = false)
    private LocalDate startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDate endAt;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EType type;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private Experience(final String title,
                       final String situation,
                       final String task,
                       final String action,
                       final String result,
                       final LocalDate startAt,
                       final LocalDate endAt,
                       final EType type,
                       final boolean isDefault,
                       final User user) {
        this.title = title;
        this.situation = situation;
        this.task = task;
        this.action = action;
        this.result = result;
        this.startAt = startAt;
        this.endAt = endAt;
        this.type = type;
        this.isDefault = isDefault;
        this.user = user;
    }

    public static Experience create(final String title,
                                    final String situation,
                                    final String task,
                                    final String action,
                                    final String result,
                                    final LocalDate startAt,
                                    final LocalDate endAt,
                                    final EType type,
                                    final boolean isDefault,
                                    final User user){
        validateDate(startAt, endAt);
        return Experience.builder()
                .title(title)
                .situation(situation)
                .task(task)
                .action(action)
                .result(result)
                .startAt(startAt)
                .endAt(endAt)
                .type(type)
                .isDefault(isDefault)
                .user(user)
                .build();
    }

    //update 메서드
    public void cancelDefault() {
        this.isDefault = false;
    }

    //validate 메서드
    public static void validateDate(LocalDate startAt, LocalDate endAt) {
        if (endAt.isBefore(startAt)) {
            log.warn("종료일시가 시작일시보다 빠를 수 없습니다. startAt: {}, endAt: {}", startAt, endAt);
            throw BaseException.type(ExperienceErrorCode.END_DATE_BEFORE_START_DATE);
        }

        if (startAt.isAfter(LocalDate.now()) || endAt.isAfter(LocalDate.now())) {
            log.warn("종료일시 및 시작일시는 미래 날짜가 될 수 없습니다. startAt: {}, endAt: {}", startAt, endAt);
            throw BaseException.type(ExperienceErrorCode.NOT_ALLOWED_FUTURE_DATE);
        }
    }

    public void update(String title,
                       String situation,
                       String task,
                       String action,
                       String result,
                       EType type,
                       LocalDate startAt,
                       LocalDate endAt,
                       boolean isDefault
                       ){
        validateDate(startAt, endAt);
        this.title = title;
        this.situation = situation;
        this.task = task;
        this.action = action;
        this.result = result;
        this.type = type;
        this.startAt = startAt;
        this.endAt = endAt;
        this.isDefault = isDefault;
    }


}
