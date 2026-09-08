package uz.insonline.travel.Travel.payload.request.reference;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name="TravelProgram")
public class TravelProgram {
    @Schema(description = "Unique id of the program")
    private Integer id;

    @Schema(description = "Name of the program")
    private String name;

    @Schema(description = "Amount of money that insurance company may cover(in euros). Per person")
    private Double liability;

    @Schema(description = "Types and amount of different coverages in case of accidents")
    private Coverages coverages;

    public TravelProgram(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    @Schema(name="Coverages")
    public static class Coverages {
        @Schema(description = "Medicine coverage amount.")
        private double medicine;

        @Schema(description = "Accident coverage amount.")
        private double accident;

        @Schema(description = "Covid coverage amount.")
        private double covid;

        @Schema(description = "Evacuation coverage amount")
        private double evacuation;

        @Schema(description = "Transportation coverage amount")
        private double transportation;

        @Schema(description = "Other compensation coverage amount")
        private double compensation;

        // --- НОВЫЕ ПОЛЯ ---
        @Schema(description = "Dentistry coverage amount")
        private double dentistry;

        @Schema(description = "Repatriation coverage amount")
        private double repatriation;

        @Schema(description = "Search and rescue coverage amount")
        private double searchAndRescue;

        @Schema(description = "Third party visit coverage amount")
        private double thirdPartyVisit;

    }
}
