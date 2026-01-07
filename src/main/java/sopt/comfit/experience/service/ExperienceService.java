package sopt.comfit.experience.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.experience.domain.EType;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.experience.domain.ExperienceRepository;
import sopt.comfit.experience.dto.command.CreateExperienceCommandDto;
import sopt.comfit.experience.dto.response.GetExperienceResponseDto;
import sopt.comfit.experience.dto.response.GetSummaryExperienceResponseDto;
import sopt.comfit.experience.exception.ExperienceErrorCode;
import sopt.comfit.global.dto.PageDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

@Service
@RequiredArgsConstructor
public class ExperienceService {

    private final ExperienceRepository experienceRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long createExperience(CreateExperienceCommandDto command){
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

        return  experience.getId();
    }

    @Transactional(readOnly = true)
    public PageDto<GetSummaryExperienceResponseDto> getSummaryExperienceList(Long userId, EType type, Pageable pageable) {

        Page<Experience> experiences = (type == null)
                ? experienceRepository.findByUserId(userId, pageable)
                : experienceRepository.findByUserIdAndType(userId, type, pageable);

        return PageDto.from(experiences.map(GetSummaryExperienceResponseDto::from));
    }

    @Transactional(readOnly = true)
    public GetExperienceResponseDto getExperience(Long userId, Long experienceId) {
        Experience experience = experienceRepository.findByIdAndUserId(experienceId, userId)
                .orElseThrow(() -> BaseException.type(ExperienceErrorCode.NOT_FOUND_EXPERIENCE));

        return GetExperienceResponseDto.from(experience);
    }

    private void cancelExistingDefault(Long userId) {
        experienceRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(Experience::cancelDefault);
    }
}
