package sopt.comfit.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sopt.comfit.auth.domain.RefreshToken;
import sopt.comfit.auth.domain.RefreshTokenRepository;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.auth.dto.command.OnBoardingCommandDto;
import sopt.comfit.auth.dto.query.LoginQueryDto;
import sopt.comfit.auth.exception.AuthErrorCode;
import sopt.comfit.auth.kakao.dto.KakaoUserApiResponseDto;
import sopt.comfit.global.constants.Constants;
import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.exception.CommonErrorCode;
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

    public JwtDto reissueToken(String refreshTokenStr) {

        if (refreshTokenStr == null) {
            throw BaseException.type(CommonErrorCode.REFRESH_TOKEN_EMPTY);
        }

        Claims claims = jwtUtil.validateToken(refreshTokenStr);

        Long userId = Long.valueOf(claims.get(Constants.CLAIM_USER_ID).toString());

        RefreshToken savedToken = refreshTokenRepository.findById(userId.toString())
                .orElseThrow(() -> BaseException.type(AuthErrorCode.REFRESH_TOKEN_EXPIRATION));

        if (!savedToken.getToken().equals(refreshTokenStr)) {
            throw BaseException.type(AuthErrorCode.REFRESH_TOKEN_EXPIRATION);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));

        refreshTokenRepository.deleteById(userId.toString());

        JwtDto jwtDto = jwtUtil.generateTokens(user.getId(), user.getRole());

        refreshTokenRepository.save(
                RefreshToken.issueRefreshToken(user.getId(), jwtDto.refreshToken())
        );

        return jwtDto;
    }

    @Transactional
    public void addUserInfo(OnBoardingCommandDto command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> BaseException.type(UserErrorCode.USER_NOT_FOUND));
        user.registerRequiredInfo(
                command.educationLevel(),
                command.firstIndustry(),
                command.secondIndustry(),
                command.thirdIndustry(),
                command.firstJob(),
                command.secondJob(),
                command.thirdJob(),
                universityRepository.findById(command.universityId())
                        .orElseThrow(() -> BaseException.type(UniversityErrorCode.UNIVERSITY_NOT_FOUND))
        );
    }

    @Transactional
    public LoginQueryDto registerOrLogin(KakaoUserApiResponseDto dto) {
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

        refreshTokenRepository.save(RefreshToken.issueRefreshToken(user.getId(), jwtDto.refreshToken()));

        return LoginQueryDto.of(user.getId(), isNew, user.getName(), jwtDto);
    }
}
