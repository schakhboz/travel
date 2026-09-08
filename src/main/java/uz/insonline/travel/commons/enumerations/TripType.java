package uz.insonline.travel.commons.enumerations;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TripType {

    ONE_WAY("ONE_WAY"),
    ROUND_TRIP("ROUND_TRIP");

    private final String name;
}
