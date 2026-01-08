package sopt.comfit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.dto.response.GetMeResponseDto;
import sopt.comfit.user.exception.UserErrorCode;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public GetMeResponseDto getMe (Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        return GetMeResponseDto.from(user);
    }
}
