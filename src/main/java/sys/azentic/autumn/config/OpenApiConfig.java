package sys.azentic.autumn.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración de OpenAPI/Swagger para documentación de la API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI autumnOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Autumn Banking System API")
                        .description("Sistema bancario con transferencias, auditoría y control de concurrencia")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Autumn Dev Team")
                                .email("dev@autumn.sys"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de desarrollo")
                ));
    }
}
