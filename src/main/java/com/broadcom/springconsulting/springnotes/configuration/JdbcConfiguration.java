package com.broadcom.springconsulting.springnotes.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.dialect.JdbcDialect;
import org.springframework.data.jdbc.core.dialect.JdbcPostgresDialect;

@Configuration
public class JdbcConfiguration {

    @Bean
    JdbcDialect jdbcDialect() {

        return new JdbcPostgresDialect();
    }

}
