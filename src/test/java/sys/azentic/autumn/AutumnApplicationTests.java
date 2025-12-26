package sys.azentic.autumn;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Test de contexto de Spring Boot con Testcontainers.
 * Testcontainers inicia automáticamente PostgreSQL y Redis en contenedores Docker.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class AutumnApplicationTests {

	@Container
	@SuppressWarnings("resource") // Testcontainers gestiona el ciclo de vida automáticamente
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
			.withDatabaseName("autumn_test")
			.withUsername("test_user")
			.withPassword("test_pass");

	@Container
	@SuppressWarnings("resource") // Testcontainers gestiona el ciclo de vida automáticamente
	static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
			.withExposedPorts(6379);

	/**
	 * Configura dinámicamente las propiedades de conexión a las bases de datos
	 * levantadas por Testcontainers.
	 */
	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		// PostgreSQL
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);

		// Redis
		registry.add("spring.data.redis.host", redis::getHost);
		registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
	}

	@Test
	void contextLoads() {
		// Este test verifica que el contexto de Spring cargue correctamente
		// con toda la configuración de JPA, Flyway, Redis, etc.
	}

}
