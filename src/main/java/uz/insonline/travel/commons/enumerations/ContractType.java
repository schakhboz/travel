package uz.insonline.travel.commons.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContractType {

    UZUM_TRAVEL("UZUM_TRAVEL"),
    CENTRUM_AIR("CENTRUM_AIR");

    private final String name;
}
