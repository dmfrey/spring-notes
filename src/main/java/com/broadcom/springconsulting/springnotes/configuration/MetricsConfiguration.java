package com.broadcom.springconsulting.springnotes.configuration;

import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfiguration {

    @Bean
    public JvmGcMetrics jvmGcMetrics() {

        return new JvmGcMetrics();
    }

}
