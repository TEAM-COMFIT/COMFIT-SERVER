package sopt.comfit.auth.dto.command;

import sopt.comfit.auth.dto.request.LoginRequestDto;

public record LoginCommandDto(
        String email,

        String password

) {

    public static LoginCommandDto from (LoginRequestDto request){
        return new  LoginCommandDto(request.email(),  request.password());
    }
}
