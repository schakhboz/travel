package uz.insonline.travel.CentrumAir.domain;

import uz.insonline.travel.CentrumAir.dto.InsurantDto;
import uz.insonline.travel.CentrumAir.dto.PassengerDto;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Контрагент учётной системы (INS_KONTRAGENT): страхователь или застрахованный.
 * У пассажира контакты и гражданство при отсутствии берутся у страхователя — как и до рефакторинга.
 */
public record Kontragent(
        String firstName,
        String lastName,
        String middleName,
        String passportSeries,
        String passportNumber,
        Integer gender,
        LocalDate birthDate,
        String phone,
        String email,
        String address,
        String pinfl,
        Integer residentType,
        Integer citizenshipId
) {

    public static Kontragent insurant(InsurantDto insurant) {
        return new Kontragent(
                insurant.firstName(), insurant.lastName(), insurant.middleName(),
                insurant.passportSeries(), insurant.passportNumber(),
                insurant.gender(), insurant.birthDate(),
                insurant.phone(), insurant.email(), insurant.address(),
                insurant.pinfl(), insurant.residentType(), insurant.citizenshipId()
        );
    }

    public static Kontragent passenger(PassengerDto passenger, InsurantDto fallback) {
        return new Kontragent(
                passenger.firstName(), passenger.lastName(), passenger.middleName(),
                passenger.passportSeries(), passenger.passportNumber(),
                passenger.gender(), passenger.birthDate(),
                Optional.ofNullable(passenger.phone()).orElse(fallback.phone()),
                Optional.ofNullable(passenger.email()).orElse(fallback.email()),
                null,
                passenger.pinfl(), passenger.residentType(),
                Optional.ofNullable(passenger.citizenshipId()).orElse(fallback.citizenshipId())
        );
    }
}
