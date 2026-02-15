package es.brasatech.fastbite.domain.payment;

import java.math.BigDecimal;

/**
 * Represents a money denomination (banknote or coin) with its value and image.
 */
public record MoneyDenomination(BigDecimal value, String image, MoneyType type) {
}
