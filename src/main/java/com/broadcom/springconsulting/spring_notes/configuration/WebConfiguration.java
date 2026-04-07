package com.broadcom.springconsulting.spring_notes.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning( ApiVersionConfigurer configurer ) {
        configurer.useRequestHeader( "API-Version" );
    }

}