package es.brasatech.fastbite.jpa.tenant;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

/** Immutable migration: future changes belong in a new version. */
public class V3__LegacyTranslations extends BaseJavaMigration {
    @Override public Integer getChecksum() { return 1; }

    @Override public void migrate(Context context) throws Exception {
        LegacyTranslationMigration.migrate(context.getConnection(), context.getConfiguration().getDefaultSchema());
    }
}
