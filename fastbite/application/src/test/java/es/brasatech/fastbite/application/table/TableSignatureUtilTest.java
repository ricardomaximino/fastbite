package es.brasatech.fastbite.application.table;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TableSignatureUtilTest {

    private final TableSignatureUtil signatures = new TableSignatureUtil("a-secret-for-tests");

    @Test
    void aCodeOpensOnlyTheTableAndRestaurantItWasMadeFor() {
        String token = signatures.generateSignature("kebab", "table-1");

        assertThat(signatures.isValid("kebab", "table-1", token)).isTrue();
        assertThat(signatures.isValid("KEBAB", "table-1", token.toUpperCase())).as("case does not matter").isTrue();
        assertThat(signatures.isValid("kebab", "table-2", token)).as("another table").isFalse();
        assertThat(signatures.isValid("burger", "table-1", token))
                .as("the same table id in another restaurant, as with the demo tables").isFalse();
    }

    @Test
    void forgedMissingOrForeignTokensAreRefused() {
        String token = signatures.generateSignature("kebab", "table-1");

        assertThat(signatures.isValid("kebab", "table-1", token.substring(1) + "0")).isFalse();
        assertThat(signatures.isValid("kebab", "table-1", null)).isFalse();
        assertThat(signatures.isValid(null, "table-1", token)).isFalse();
        assertThat(new TableSignatureUtil("another-secret").isValid("kebab", "table-1", token))
                .as("signed with another secret").isFalse();
    }
}
