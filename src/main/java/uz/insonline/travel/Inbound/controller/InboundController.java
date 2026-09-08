package uz.insonline.travel.Inbound.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.insonline.travel.Travel.payload.response.ContractResponse;
import uz.insonline.travel.Travel.payload.response.PolicyCreateResponse;
import uz.insonline.travel.Inbound.payload.request.InboundCalcRequest;
import uz.insonline.travel.Inbound.payload.request.InboundCreateRequest;
import uz.insonline.travel.Inbound.payload.response.CalculatorResponse;
import uz.insonline.travel.Inbound.payload.response.InboundProgramResponse;
import uz.insonline.travel.Inbound.service.InboundService;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/inbound")
@RequiredArgsConstructor
@Tag(name = "Inbound Travel API", description = "API для продукта 'Въездной туризм'")
public class InboundController {

    private final InboundService inboundService;

    @GetMapping("/programs")
    @Operation(summary = "Получить список тарифов")
    public ResponseEntity<List<InboundProgramResponse>> getPrograms() {
        return ResponseEntity.ok(inboundService.getPrograms());
    }

    @PostMapping("/calculate")
    @Operation(summary = "Калькулятор премии")
    public ResponseEntity<CalculatorResponse> calculate(@RequestBody @Valid InboundCalcRequest request) {
        CalculatorResponse response = inboundService.calculator(request);

        if (response.getResult() == 0) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/create")
    @Operation(summary = "Создание контракта")
    public ResponseEntity<ContractResponse> create(@RequestBody @Valid InboundCreateRequest request) {

        ContractResponse response = inboundService.create(request);

        if (response.getResult() == 0) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        }

        if (response.getResult_message() != null &&
                (response.getResult_message().contains("уже застрахован") ||
                        response.getResult_message().contains("already insured") ||
                        response.getResult() == -20106)) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(response);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

/*
    @PostMapping("/activate")
    @Operation(summary = "Активация полиса (Временно)")
    public ResponseEntity<ContractResponse> activate(@RequestParam Long contractId) {
        ContractResponse response = inboundService.activate(contractId);
        if (response.getResult() == 0) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
*/

    @GetMapping("/get-policy-link")
    @Operation(summary = "Получить ссылку на полис")
    public ResponseEntity<PolicyCreateResponse> getPolicyLink(@RequestParam Long contractId) {
        return ResponseEntity.ok(inboundService.getPolicyLink(contractId));
    }
}