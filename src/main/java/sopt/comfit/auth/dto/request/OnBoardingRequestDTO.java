package sopt.comfit.auth.dto.request;

public record OnBoardingRequestDTO (
        String educationLevel,
        String firstIndustry,
        String secondIndustry,
        String thirdIndustry,
        String firstJob,
        String secondJob,
        String thirdJob,
        Long universityId
) {
}
