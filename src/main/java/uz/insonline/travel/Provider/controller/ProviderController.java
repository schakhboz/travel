package uz.insonline.travel.Provider.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.insonline.travel.Provider.payload.reponse.PersonBirthDateResponseDto;
import uz.insonline.travel.Provider.payload.request.PersonBirthDateRequestDto;
import uz.insonline.travel.Provider.service.ProviderService;

@RestController
@RequestMapping("/api/provider")
@RequiredArgsConstructor
@Tag(name = "Provider", description = "APIs for get necessary info")
public class ProviderController {
    private final ProviderService service;

    @Operation(summary = "Get person information by birthDate,passportSeries and passportNumber")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "completed successfully",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = PersonBirthDateResponseDto.class))})})
    @Cacheable("provider-passport-birth-date")
    @PostMapping("/passport-birth-date")
    public HttpEntity<?> personBirthDay(@Valid @RequestBody PersonBirthDateRequestDto dto) {
        return ResponseEntity.ok(service.personBirthDay(dto));
    }
}
