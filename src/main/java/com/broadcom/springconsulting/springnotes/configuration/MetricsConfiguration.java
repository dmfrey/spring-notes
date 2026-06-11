package com.broadcom.springconsulting.springnotes.configuration;

import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import org.springframework.aot.hint.ExecutableMode;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.List;

@Configuration
@ImportRuntimeHints(MetricsConfiguration.ProtobufRuntimeHints.class)
public class MetricsConfiguration {

    @Bean
    public JvmGcMetrics jvmGcMetrics() {

        return new JvmGcMetrics();
    }

    /**
     * micrometer-registry-otlp initializes protobuf via reflection
     * (ExtensionRegistry.getEmptyRegistry), which is not covered by GraalVM
     * reachability metadata and fails at runtime in the native image.
     */
    static class ProtobufRuntimeHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {

            hints.reflection().registerType(
                    TypeReference.of("com.google.protobuf.ExtensionRegistry"),
                    type -> type.withMethod("getEmptyRegistry", List.of(), ExecutableMode.INVOKE));
        }

    }

}
