package sopt.comfit.company.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

import static sopt.comfit.global.constants.Constants.BASE_URL;

@Getter
@RequiredArgsConstructor
public enum ERandomPhoto {
    RAN1(BASE_URL + "random1.png"),
    RAN2(BASE_URL + "random2.png"),
    RAN3(BASE_URL + "random3.png"),
    RAN4(BASE_URL + "random4.png"),
    RAN5(BASE_URL + "random5.png"),
    RAN6(BASE_URL + "random6.png"),
    RAN7(BASE_URL + "random7.png"),
    RAN8(BASE_URL + "random8.png"),
    RAN9(BASE_URL + "random9.png"),
    RAN10(BASE_URL + "random10.png"),
    RAN11(BASE_URL + "random11.png");

    private final String photoUrl;

    private static final ERandomPhoto[] VALUES = values();

    public static ERandomPhoto random() {
        return VALUES[ThreadLocalRandom.current().nextInt(VALUES.length)];
    }
}
