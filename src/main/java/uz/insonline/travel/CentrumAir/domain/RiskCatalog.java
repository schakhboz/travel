package uz.insonline.travel.CentrumAir.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Состав рисков по учётным группам (ТЗ п. 3–4) и привязка риска к справочникам учётной системы.
 * Чистые функции без обращения к БД.
 */
public final class RiskCatalog {

    public static final String TRAVEL = "TRAVEL";
    public static final String ACCIDENT = "ACCIDENT";
    public static final String BAGGAGE = "BAGGAGE";
    public static final String ADDON_BAGGAGE = "ADDON_BAGGAGE";
    public static final String ANIMAL = "ANIMAL";
    public static final String CANCEL = "CANCEL";
    public static final String DELAY = "DELAY";
    public static final String DOCS = "DOCS";

    private RiskCatalog() {
    }

    /** Риски, попадающие в полис учётной группы. Риски разных групп в один полис не объединяются. */
    public static List<String> risksForGroup(int policyGroup, ProductSelection products) {
        String packageCode = products.packageCode();
        return switch (policyGroup) {
            case 0 -> products.has(ProductSelection.TRAVEL) ? List.of(TRAVEL) : List.of();
            case 1 -> {
                List<String> risks = new ArrayList<>();
                risks.add(ACCIDENT);
                if ("EXTENDED".equals(packageCode) || "MAXIMUM".equals(packageCode)) {
                    risks.add(BAGGAGE);
                }
                if (products.quantity(ProductSelection.ADDON_BAGGAGE) > 0) {
                    risks.add(ADDON_BAGGAGE);
                }
                if (products.quantity(ProductSelection.ANIMAL) > 0) {
                    risks.add(ANIMAL);
                }
                yield risks;
            }
            case 2 -> packageCode != null ? List.of(CANCEL) : List.of();
            case 3 -> "MAXIMUM".equals(packageCode) ? List.of(DOCS, DELAY) : List.of(DOCS);
            default -> List.of();
        };
    }

    /** Код тарифа, по которому считается доля риска в премии полиса. */
    public static String tariffCodeFor(String riskCode, String packageCode) {
        return switch (riskCode) {
            case TRAVEL, ADDON_BAGGAGE, ANIMAL -> riskCode;
            default -> packageCode;
        };
    }

    /** Код риска так, как он хранится в INS_PASSENGER_RISK_DETAILS. */
    public static String storageCode(String riskCode) {
        return ADDON_BAGGAGE.equals(riskCode) ? BAGGAGE : riskCode;
    }

    public static Long linkIdFor(String riskCode) {
        return switch (riskCode) {
            case TRAVEL -> 602L;
            case ACCIDENT -> 592L;
            case BAGGAGE, ADDON_BAGGAGE, ANIMAL -> 603L;
            case CANCEL, DELAY, DOCS -> 593L;
            default -> throw new IllegalArgumentException("Unknown risk code: " + riskCode);
        };
    }

    /** Для этих рисков объектом страхования выступает застрахованный (INS_TRAVEL), а не запись о риске. */
    public static boolean boundToTravel(String riskCode) {
        return TRAVEL.equals(riskCode) || ACCIDENT.equals(riskCode);
    }
}
