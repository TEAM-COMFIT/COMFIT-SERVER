package sopt.comfit.user.dto;

import sopt.comfit.user.domain.ERole;
import sopt.comfit.user.domain.User;

public interface UserSecurityForm {
    Long getUserId();
    ERole getRole();
    String getPassword();

    static UserSecurityForm invoke(User user){
        return new UserSecurityForm() {
            @Override
            public Long getUserId() {
                return user.getId();
            }
            @Override
            public ERole getRole() {
                return user.getRole();
            }
            @Override
            public String getPassword() {
                return null;
            }
        };
    }
}
