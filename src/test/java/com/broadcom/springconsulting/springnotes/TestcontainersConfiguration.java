package com.broadcom.springconsulting.springnotes;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

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
	LgtmStackContainer grafanaLgtmContainer() {
		return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:latest"));
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(
				DockerImageName.parse("pgvector/pgvector:pg18").asCompatibleSubstituteFor("postgres"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management"));
	}

	// llama3.2:1b (chat) and nomic-embed-text (embedding) are both small purely to keep
	// local/CI runs fast; this is a wiring check, not a quality check. nomic-embed-text
	// specifically must stay 768-dimensional to match vector_store's embedding vector(768)
	// column (db.changelog-chat-002.yaml) - swapping it here without also changing that
	// migration would break every add()/similaritySearch() call with a dimension mismatch.
	// Pulled fresh on every start for now; a later pass should adopt Testcontainers'
	// commitToImage() caching pattern to avoid the repeated pull cost.
	@Bean
	@ServiceConnection
	OllamaContainer ollamaContainer() throws IOException, InterruptedException {
		var container = new OllamaContainer( DockerImageName.parse( "ollama/ollama:latest" ) );
		container.start();
		container.execInContainer( "ollama", "pull", "llama3.2:1b" );
		container.execInContainer( "ollama", "pull", "nomic-embed-text" );
		return container;
	}

}
