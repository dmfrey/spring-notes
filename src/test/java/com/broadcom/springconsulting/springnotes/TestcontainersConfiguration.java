package com.broadcom.springconsulting.springnotes;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

// Full container set for a real application boot (@SpringBootTest) - composes
// DataTestcontainersConfiguration (Postgres + the Jackson/Observation fallback beans) with the
// services only a full context needs: RabbitMQ (event publishing/listeners), Ollama (chat), and
// the Grafana LGTM stack (tracing/metrics export). A @DataJdbcTest slice should import
// DataTestcontainersConfiguration directly instead of this one - it doesn't need any of the three.
@TestConfiguration(proxyBeanMethods = false)
@Import(DataTestcontainersConfiguration.class)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	LgtmStackContainer grafanaLgtmContainer() {
		return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:latest"));
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
