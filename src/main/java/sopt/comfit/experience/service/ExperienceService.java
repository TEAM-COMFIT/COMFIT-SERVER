package sopt.comfit.experience.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.experience.domain.Experience;
import sopt.comfit.experience.domain.ExperienceRepository;
import sopt.comfit.experience.dto.command.CreateExperienceCommandDto;
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

    private void cancelExistingDefault(Long userId) {
        experienceRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(Experience::cancelDefault);
    }
}
