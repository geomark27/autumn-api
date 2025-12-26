package sys.azentic.autumn.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

import java.time.Duration;

/**
 * Configuración de Redis para manejo de idempotencia.
 * Redis se usa como cache distribuido para almacenar claves de idempotencia
 * y evitar procesamiento duplicado de transferencias.
 */
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    @Value("${autumn.idempotency.ttl-hours:24}")
    private long idempotencyTtlHours;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Usar StringRedisSerializer para las claves
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Usar GenericToStringSerializer para los valores
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        template.setHashValueSerializer(new GenericToStringSerializer<>(Object.class));
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Retorna la duración del TTL para claves de idempotencia.
     */
    @Bean
    public Duration idempotencyTtl() {
        return Duration.ofHours(idempotencyTtlHours);
    }
}
