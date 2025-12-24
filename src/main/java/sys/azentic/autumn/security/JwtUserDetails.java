package sys.azentic.autumn.security;

import lombok.Data;

/**
 * Placeholder para futura implementación de JWT.
 * 
 * Esta clase representará un token JWT decodificado con la información del usuario.
 * Se usará después de validar el token en el JwtAuthenticationFilter.
 */
@Data
public class JwtUserDetails {
    
    private String userId;
    private String email;
    private String role;
    
    // TODO: Implementar UserDetails de Spring Security cuando se active JWT
    // implements UserDetails
}
