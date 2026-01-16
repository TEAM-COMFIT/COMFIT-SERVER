package sopt.comfit.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.auth.domain.RefreshToken;
import sopt.comfit.auth.domain.RefreshTokenRepository;
import sopt.comfit.auth.dto.UserInfoDto;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.request.OnBoardingRequestDTO;
import sopt.comfit.auth.kakao.dto.KakaoUserApiResponseDto;
import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.security.util.JwtUtil;
import sopt.comfit.university.domain.UniversityRepository;
import sopt.comfit.university.exception.UniversityErrorCode;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

import java.util.Optional;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UniversityRepository universityRepository;

    public JwtDto login(LoginCommandDto command) {
        log.info("로그인 시작");
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        JwtDto jwtDto = jwtUtil.generateTokens(user.getId(), user.getRole());

        refreshTokenRepository.save(RefreshToken.issueRefreshToken(user.getId(), jwtDto.refreshToken()));

        return jwtDto;
    }


    public void logout(Long userId){
        refreshTokenRepository.deleteById(userId.toString());
    }

    @Transactional
    public void addUserInfo(Long userId, OnBoardingRequestDTO request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
        user.registerRequiredInfo(
                request.educationLevel(),
                request.firstIndustry(),
                request.secondIndustry(),
                request.thirdIndustry(),
                request.firstJob(),
                request.secondJob(),
                request.thirdJob(),
                universityRepository.findById(request.universityId())
                        .orElseThrow(() -> BaseException.type(UniversityErrorCode.UNIVERSITY_NOT_FOUND))
        );
    }

    @Transactional
    public UserInfoDto registerOrLogin(KakaoUserApiResponseDto dto) {
        Optional<User> optionalUser =
                userRepository.findByEmail(dto.kakao_account().email());

        boolean isNew = optionalUser.isEmpty();

        User user = optionalUser.orElseGet(() ->
                userRepository.save(
                        User.createKakaoUser(
                                dto.kakao_account().email(),
                                String.valueOf(dto.id()),
                                dto.kakao_account().profile().nickname()
                        )
                )
        );

        JwtDto jwtDto = jwtUtil.generateTokens(user.getId(), user.getRole());
        return UserInfoDto.from(user, isNew, jwtDto);
    }
}
