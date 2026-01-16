package sopt.comfit.experience.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.experience.domain.ExperienceRepository;
import sopt.comfit.experience.dto.command.CreateExperienceCommandDto;
import sopt.comfit.experience.dto.command.UpdateDefaultCommandDto;
import sopt.comfit.experience.dto.command.UpdateExperienceCommandDto;
import sopt.comfit.experience.dto.response.GetExperienceResponseDto;
import sopt.comfit.experience.dto.response.GetSummaryExperienceResponseDto;
import sopt.comfit.experience.exception.ExperienceErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createExperience(CreateExperienceCommandDto command){
        log.info("경험 생성 시작 - userId: {}", command.userId());

        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        if(command.isDefault()) {
            cancelExistingDefault(command.userId());
        }

        Experience experience = experienceRepository.save(Experience.create(
                command.title(),
                command.situation(),
                command.task(),
                command.action(),
                command.result(),
                command.startAt(),
                command.endAt(),
                command.type(),
                command.isDefault(),
                user));

        log.info("경험 생성 완료 - experienceId: {}", experience.getId());
        return  experience.getId();
    }

    @Transactional(readOnly = true)
    public PageDto<GetSummaryExperienceResponseDto> getSummaryExperienceList(Long userId, EType type, Pageable pageable) {

        Page<Experience> experiences = (type == null)
                ? experienceRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId, pageable)
                : experienceRepository.findByUserIdAndTypeOrderByIsDefaultDescCreatedAtDesc(userId, type, pageable);

        return PageDto.from(experiences.map(GetSummaryExperienceResponseDto::from));
    }

    @Transactional(readOnly = true)
    public GetExperienceResponseDto getExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
                .orElseThrow(() -> BaseException.type(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        return GetExperienceResponseDto.from(experience);
    }

    @Transactional
    public Long updateExperience(UpdateExperienceCommandDto command){
        Experience experience = experienceRepository.findByIdAndUserId(command.experienceId(), command.userId())
                .orElseThrow(() -> BaseException.type(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        if(command.isDefault() && !experience.isDefault()) {
            cancelExistingDefault(command.userId());
        }

        log.info("경험 업데이트 시작: experienceId: {}", experience.getId());
        experience.update(
                command.title(),
                command.situation(),
                command.task(),
                command.action(),
                command.result(),
                command.type(),
                command.startAt(),
                command.endAt(),
                command.isDefault());

        log.info("경험 업데이트 완료: experienceId: {}", experience.getId());
        return experience.getId();
    }

    @Transactional
    public void deleteExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
                .orElseThrow(() -> BaseException.type(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        experienceRepository.delete(experience);
        log.info("경험 삭제 완료 - userId: {}, experienceId: {}", userId, experienceId);
    }

    @Transactional
    public void updateDefault(UpdateDefaultCommandDto command){
        Experience experience = experienceRepository.findByIdAndUserId(command.experienceId(), command.userId())
                .orElseThrow(() -> BaseException.type(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        if(experience.isDefault()){
            experience.cancelDefault();
            return;
        }

        cancelExistingDefault(command.userId());

        experience.activateDefault();
    }

    private void cancelExistingDefault(Long userId) {
        experienceRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(Experience::cancelDefault);
    }
}
