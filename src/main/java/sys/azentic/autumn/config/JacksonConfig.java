package sys.azentic.autumn.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Jackson ObjectMapper.
 * Necesario para serialización JSON en AuditService y Redis.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar módulo para soporte de Java 8 Time API (LocalDateTime, etc.)
        mapper.registerModule(new JavaTimeModule());
        
        // Escribir fechas como strings ISO-8601 en lugar de timestamps
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        return mapper;
    }
}
