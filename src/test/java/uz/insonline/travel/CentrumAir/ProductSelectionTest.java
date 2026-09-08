package uz.insonline.travel.CentrumAir;

import org.junit.jupiter.api.Test;
import uz.insonline.travel.CentrumAir.domain.ProductSelection;
import uz.insonline.travel.CentrumAir.domain.RiskCatalog;
import uz.insonline.travel.CentrumAir.dto.ProductDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductSelectionTest {

    @Test
    void readsPackageQuantitiesAndFingerprint() {
        ProductSelection products = ProductSelection.of(List.of(
                new ProductDto("EXTENDED", null),
                new ProductDto("ADDON_BAGGAGE", 2),
                new ProductDto("TRAVEL", null)
        ));

        assertEquals("EXTENDED", products.packageCode());
        assertEquals(2, products.quantity("ADDON_BAGGAGE"));
        assertEquals(0, products.quantity("ANIMAL"));
        assertTrue(products.has("travel"));
        assertEquals("ADDON_BAGGAGE:2|EXTENDED:0|TRAVEL:0", products.fingerprint());
    }

    @Test
    void fingerprintIgnoresProductOrder() {
        ProductSelection first = ProductSelection.of(List.of(
                new ProductDto("STANDARD", null), new ProductDto("ANIMAL", 1)));
        ProductSelection second = ProductSelection.of(List.of(
                new ProductDto("ANIMAL", 1), new ProductDto("STANDARD", null)));

        assertEquals(first.fingerprint(), second.fingerprint());
    }

    @Test
    void groupsRisksAccordingToPurchasedProducts() {
        ProductSelection maximum = ProductSelection.of(List.of(
                new ProductDto("MAXIMUM", null), new ProductDto("ANIMAL", 1), new ProductDto("TRAVEL", null)));

        assertEquals(List.of("TRAVEL"), RiskCatalog.risksForGroup(0, maximum));
        assertEquals(List.of("ACCIDENT", "BAGGAGE", "ANIMAL"), RiskCatalog.risksForGroup(1, maximum));
        assertEquals(List.of("CANCEL"), RiskCatalog.risksForGroup(2, maximum));
        assertEquals(List.of("DOCS", "DELAY"), RiskCatalog.risksForGroup(3, maximum));

        ProductSelection standard = ProductSelection.of(List.of(new ProductDto("STANDARD", null)));
        assertEquals(List.of(), RiskCatalog.risksForGroup(0, standard));
        assertEquals(List.of("ACCIDENT"), RiskCatalog.risksForGroup(1, standard));
        assertEquals(List.of("DOCS"), RiskCatalog.risksForGroup(3, standard));
    }
}
