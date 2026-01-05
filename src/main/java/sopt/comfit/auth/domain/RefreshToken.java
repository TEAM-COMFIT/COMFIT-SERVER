package sopt.comfit.auth.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RedisHash(value = "token", timeToLive = 60 * 60 * 24)
public class RefreshToken {

    @Id
    private String id;

    @Indexed
    private String token;

    private RefreshToken(String id, String token) {
        this.id = id;
        this.token = token;
    }

    public static RefreshToken issueRefreshToken(
            final Long userId,
            final String refreshToken
    ){
        return new RefreshToken(userId.toString(), refreshToken);
    }
}
