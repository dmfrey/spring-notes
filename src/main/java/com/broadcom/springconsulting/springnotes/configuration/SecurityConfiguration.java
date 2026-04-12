package com.broadcom.springconsulting.springnotes.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain( HttpSecurity http ) {

        http
            .authorizeHttpRequests( auth -> auth
                .requestMatchers( "/actuator/health/**", "/actuator/info", "/actuator/sbom/**" ).permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer( oauth2 -> oauth2.jwt( Customizer.withDefaults() ) )
            .sessionManagement( session -> session
                .sessionCreationPolicy( SessionCreationPolicy.STATELESS )
            )
            .csrf( AbstractHttpConfigurer::disable );

        return http.build();
    }

}