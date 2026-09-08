package uz.insonline.travel.Inbound.payload.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ResidentContactValidator.class)
public @interface ValidResidentContact {
    String message() default "Ошибка контактных данных";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}