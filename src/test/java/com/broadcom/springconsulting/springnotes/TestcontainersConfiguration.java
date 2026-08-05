package com.broadcom.springconsulting.springnotes;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.grafana.LgtmStackContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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

	@Bean
	@ServiceConnection
	LgtmStackContainer grafanaLgtmContainer() {
		return new LgtmStackContainer(DockerImageName.parse("grafana/otel-lgtm:latest"));
	}

	@Bean
	@ServiceConnection
	PostgreSQLContainer postgresContainer() {
		return new PostgreSQLContainer(DockerImageName.parse("postgres:latest"));
	}

	@Bean
	@ServiceConnection
	RabbitMQContainer rabbitMQContainer() {
		return new RabbitMQContainer(DockerImageName.parse("rabbitmq:4-management"));
	}

}
