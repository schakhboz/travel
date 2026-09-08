package uz.insonline.travel.commons.annotation.TravelV2;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ContractRequestV2Validator.class)
public @interface TravelRequestValidator {

    String message() default "Invalid contract request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}