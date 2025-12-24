package sys.azentic.autumn.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuración de auditoría JPA.
 * Habilita @CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
    
    // Spring Boot configurará automáticamente AuditingEntityListener
    // Las entidades con @EntityListeners(AuditingEntityListener.class)
    // tendrán sus campos de auditoría poblados automáticamente
}
