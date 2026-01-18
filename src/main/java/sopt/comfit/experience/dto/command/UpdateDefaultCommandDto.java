package sopt.comfit.experience.dto.command;

public record UpdateDefaultCommandDto(
        Long userId,

        Long experienceId
) {
    public static UpdateDefaultCommandDto of(Long userId, Long experienceId) {
        return new UpdateDefaultCommandDto(userId, experienceId);
    }
}
