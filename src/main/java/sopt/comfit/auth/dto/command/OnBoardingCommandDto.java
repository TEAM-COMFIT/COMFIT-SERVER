package sopt.comfit.auth.dto.command;

import sopt.comfit.auth.dto.request.OnBoardingRequestDTO;
import sopt.comfit.global.enums.EIndustry;
import sopt.comfit.user.domain.EEducationLevel;
import sopt.comfit.user.domain.EJob;

public record OnBoardingCommandDto(

        Long userId,

        EEducationLevel educationLevel,

        EIndustry firstIndustry,

        EIndustry secondIndustry,

        EIndustry thirdIndustry,

        EJob firstJob,

        EJob secondJob,

        EJob thirdJob,

        Long universityId

) {
    public static OnBoardingCommandDto of(Long userId, OnBoardingRequestDTO request) {

        return new OnBoardingCommandDto(
                userId,
                EEducationLevel.from(request.educationLevel()),
                EIndustry.from(request.firstIndustry()),
                EIndustry.from(request.secondIndustry()),
                EIndustry.from(request.thirdIndustry()),
                EJob.from(request.firstJob()),
                EJob.from(request.secondJob()),
                EJob.from(request.thirdJob()),
                request.universityId()
        );
    }
}
