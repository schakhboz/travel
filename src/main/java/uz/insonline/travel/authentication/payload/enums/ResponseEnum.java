package uz.insonline.travel.authentication.payload.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseEnum {

    BEARER("Bearer "),
    BASIC("Basic "),
    SUCCESS("Succes"),
    YOUR_TOKEN("Your token"),

    ERROR("error");
    private final String text;
}
