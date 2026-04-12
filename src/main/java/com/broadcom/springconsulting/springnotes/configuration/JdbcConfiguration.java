package com.broadcom.springconsulting.springnotes.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Configuration
@EnableJdbcAuditing
public class JdbcConfiguration {

    @Bean
    JdbcDialect jdbcDialect() {

        return new JdbcPostgresDialect();
    }

    @Component
    static class AuditorAwareImpl implements AuditorAware<String> {

        @Override
        public Optional<String> getCurrentAuditor() {

            var authentication = SecurityContextHolder.getContext().getAuthentication();

            if( authentication == null || !authentication.isAuthenticated() ||
                    Objects.equals( authentication.getPrincipal(), "anonymousUser" ) ) {

                return Optional.empty();
            }

            // Assuming JWT username is stored as the principal name
            return Optional.of( authentication.getName() );
        }

    }

}
