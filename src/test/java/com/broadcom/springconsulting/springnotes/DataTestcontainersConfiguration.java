package com.broadcom.springconsulting.springnotes;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

// Just the data-layer services - Postgres plus the two fallback beans a @DataJdbcTest slice
// needs but doesn't autoconfigure. Import this (not the full TestcontainersConfiguration) from a
// @DataJdbcTest so it doesn't also spin up RabbitMQ/Ollama/Grafana LGTM, none of which it touches.
@TestConfiguration(proxyBeanMethods = false)
public class DataTestcontainersConfiguration {

	// @DataJdbcTest slices don't autoconfigure Jackson, but NoteEventStoreAdapter needs an
	// ObjectMapper. @ConditionalOnMissingBean is safe here (unlike in a real @Configuration
	// class) because this class is @TestConfiguration - it never ships in the production JAR,
	// so there's no risk of it racing JacksonAutoConfiguration's bean in the real app.
	@Bean
	@ConditionalOnMissingBean(ObjectMapper.class)
	ObjectMapper objectMapper() {
		return JsonMapper.builder().build();
	}

	// @DataJdbcTest slices don't autoconfigure Micrometer Observation either, but
	// NotesConfiguration's component scan pulls in every application service (LoadNotesService,
	// CreateNoteService, etc.), all of which take an ObservationRegistry constructor parameter.
	@Bean
	@ConditionalOnMissingBean(ObservationRegistry.class)
	ObservationRegistry observationRegistry() {
		return ObservationRegistry.NOOP;
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(
				DockerImageName.parse("pgvector/pgvector:pg18").asCompatibleSubstituteFor("postgres"));
	}

}
