package uz.insonline.travel.Inbound.payload.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uz.insonline.travel.Inbound.payload.request.InboundCreateRequest.PersonData;

public class ResidentContactValidator implements ConstraintValidator<ValidResidentContact, PersonData> {

    @Override
    public boolean isValid(PersonData data, ConstraintValidatorContext context) {
        if (data == null || data.getResidentId() == null) {
            return true;
        }

        boolean isValid = true;
        Integer residentId = data.getResidentId();


        if (residentId == 1 || residentId == 5) {


            if (data.getPhone() == null || data.getPhone().trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Для резидентов (тип 1 или 5) телефон обязателен")
                        .addPropertyNode("phone")
                        .addConstraintViolation();
                isValid = false;
            }


            if (data.getPinfl() == null || data.getPinfl().trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Для резидентов (тип 1 или 5) ПИНФЛ обязателен")
                        .addPropertyNode("pinfl")
                        .addConstraintViolation();
                isValid = false;
            }
        }


        if (residentId == 2) {

            if (data.getEmail() == null || data.getEmail().trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Для нерезидентов (тип 2) email обязателен")
                        .addPropertyNode("email")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }
}