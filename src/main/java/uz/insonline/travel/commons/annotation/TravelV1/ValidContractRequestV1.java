package uz.insonline.travel.commons.annotation.TravelV1;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateContractRequestValidatorV1.class)
public @interface ValidContractRequestV1 {

    String message() default "Invalid contract create request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
