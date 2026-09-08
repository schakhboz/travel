package uz.insonline.travel.CentrumAir.domain;

import uz.insonline.travel.CentrumAir.dto.ProductDto;
import uz.insonline.travel.CentrumAir.error.CentrumAirApiException;

import java.util.List;

/**
 * Допустимость комбинации продуктов (ТЗ п. 7.6.2). Одни и те же правила действуют
 * и при выпуске полисов, и в калькуляторе для партнёров.
 */
public final class ProductRules {

    private ProductRules() {
    }

    public static void validate(List<ProductDto> products, boolean international, boolean roundTrip) {
        ProductSelection selection = ProductSelection.of(products);

        if (countOf(products, ProductSelection.TRAVEL) > 1) {
            throw CentrumAirApiException.validation("Only one TRAVEL product per booking is allowed");
        }
        if (packageCount(products) > 1) {
            throw CentrumAirApiException.validation("Only one aviation package per booking is allowed");
        }
        if (selection.has(ProductSelection.TRAVEL) && !(international && roundTrip)) {
            throw CentrumAirApiException.validation("TRAVEL is available for international round-trip routes only");
        }
        if (selection.packageCode() == null
                && (selection.has(ProductSelection.ANIMAL) || selection.has(ProductSelection.ADDON_BAGGAGE))) {
            throw CentrumAirApiException.validation(
                    "ANIMAL and ADDON_BAGGAGE require an aviation package in the same booking");
        }
    }

    private static long countOf(List<ProductDto> products, String productCode) {
        return products.stream()
                .filter(product -> productCode.equalsIgnoreCase(product.productCode()))
                .count();
    }

    private static long packageCount(List<ProductDto> products) {
        return products.stream()
                .filter(product -> ProductSelection.of(List.of(product)).packageCode() != null)
                .count();
    }
}
