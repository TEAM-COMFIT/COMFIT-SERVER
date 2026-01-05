package sopt.comfit.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sopt.comfit.auth.domain.RefreshToken;
import sopt.comfit.auth.domain.RefreshTokenRepository;
import sopt.comfit.auth.dto.command.LoginCommandDto;
import sopt.comfit.global.dto.JwtDto;
import sopt.comfit.global.exception.BaseException;
import sopt.comfit.global.security.util.JwtUtil;
import sopt.comfit.user.domain.User;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.exception.UserErrorCode;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

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
}
