package com.broadcom.springconsulting.springnotes.configuration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNativeImage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnNativeImage
public class NativeImageMetricsConfiguration {

    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics() {
            @Override
            public void bindTo(MeterRegistry registry) {
                // GarbageCollectorMXBean notifications are not available in native images
            }
        };
    }

}
