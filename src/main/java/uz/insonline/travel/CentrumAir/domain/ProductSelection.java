package uz.insonline.travel.CentrumAir.domain;

import uz.insonline.travel.CentrumAir.dto.ProductDto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Купленные продукты брони в виде «код → количество».
 * Заменяет разъехавшиеся по сервисам хелперы hasProduct / getProductQuantity / getSelectedPackage.
 */
public record ProductSelection(Map<String, Integer> quantities) {

    public static final String TRAVEL = "TRAVEL";
    public static final String ADDON_BAGGAGE = "ADDON_BAGGAGE";
    public static final String ANIMAL = "ANIMAL";

    private static final Set<String> PACKAGE_CODES = Set.of("STANDARD", "EXTENDED", "MAXIMUM");

    public static ProductSelection of(List<ProductDto> products) {
        if (products == null || products.isEmpty()) {
            return new ProductSelection(Map.of());
        }
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (ProductDto product : products) {
            quantities.merge(normalize(product.productCode()),
                    product.quantity() != null ? product.quantity() : 0,
                    Integer::sum);
        }
        return new ProductSelection(quantities);
    }

    public boolean has(String productCode) {
        return quantities.containsKey(normalize(productCode));
    }

    public int quantity(String productCode) {
        return quantities.getOrDefault(normalize(productCode), 0);
    }

    /** Выбранный авиа-пакет (STANDARD / EXTENDED / MAXIMUM) или null, если пакет не куплен. */
    public String packageCode() {
        return quantities.keySet().stream()
                .filter(PACKAGE_CODES::contains)
                .findFirst()
                .orElse(null);
    }

    /** Стабильный отпечаток набора продуктов — часть ключа идемпотентности (ТЗ п. 7.3). */
    public String fingerprint() {
        return quantities.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining("|"));
    }

    private static String normalize(String productCode) {
        return productCode == null ? "" : productCode.trim().toUpperCase();
    }
}
