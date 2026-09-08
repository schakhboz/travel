package uz.insonline.travel.commons.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TariffCode {

    STANDARD("STANDARD"),
    EXTENDED("EXTENDED"),
    MAXIMUM("MAXIMUM");

    private final String name;
}
