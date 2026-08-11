package com.broadcom.springconsulting.springnotes.configuration;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

@Configuration
@ImportRuntimeHints( LiquibaseConfiguration.ChangeRuntimeHints.class )
public class LiquibaseConfiguration {

    /**
     * Liquibase reflectively invokes each Change/config class's bean-property getters (e.g.
     * AddUniqueConstraintChange.getDeferrable()) to compute a changeset's checksum - not just
     * the first time a changeset runs, but to re-validate it against DATABASECHANGELOG on every
     * subsequent startup too. GraalVM's default reachability metadata for Liquibase doesn't
     * cover every Change subtype this app's changelogs use, so an unregistered one crashes the
     * native image on every fresh pod start, even though the changeset already applied
     * successfully the first time (that path doesn't hit the same reflective checksum
     * re-validation code).
     */
    static class ChangeRuntimeHints implements RuntimeHintsRegistrar {

        private static final String[] TYPES = {
                "liquibase.change.core.CreateTableChange",
                "liquibase.change.core.AddColumnChange",
                "liquibase.change.core.AddUniqueConstraintChange",
                "liquibase.change.core.CreateIndexChange",
                "liquibase.change.core.RawSQLChange",
                "liquibase.change.ColumnConfig",
                "liquibase.change.AddColumnConfig",
                "liquibase.change.ConstraintsConfig"
        };

        @Override
        public void registerHints( RuntimeHints hints, ClassLoader classLoader ) {

            for ( String type : TYPES ) {
                hints.reflection().registerType(
                        TypeReference.of( type ),
                        MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
                        MemberCategory.INVOKE_PUBLIC_METHODS );
            }

        }

    }

}
