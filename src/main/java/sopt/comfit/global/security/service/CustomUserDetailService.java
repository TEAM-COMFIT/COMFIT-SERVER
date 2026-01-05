package sopt.comfit.global.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import sopt.comfit.global.security.info.UserPrincipal;
import sopt.comfit.user.domain.UserRepository;
import sopt.comfit.user.dto.UserSecurityForm;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailService {

    private final UserRepository userRepository;

    public UserPrincipal loadUserById(Long id){
        UserSecurityForm memberSecurityForm = userRepository.findUserSecurityFormById(id)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        return UserPrincipal.create(memberSecurityForm);
    }
}
