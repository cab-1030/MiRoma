package com.miroma.miroma.service;

import com.miroma.miroma.dto.CheckUserResponse;
import com.miroma.miroma.dto.LinkPartnerRequest;
import com.miroma.miroma.dto.LinkPartnerResponse;
import com.miroma.miroma.dto.LoginRequest;
import com.miroma.miroma.dto.LoginResponse;
import com.miroma.miroma.dto.RegisterRequest;
import com.miroma.miroma.dto.RegisterResponse;
import com.miroma.miroma.entity.Pareja;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.exception.ResourceNotFoundException;
import com.miroma.miroma.exception.UnauthorizedException;
import com.miroma.miroma.exception.ValidationException;
import com.miroma.miroma.repository.UsuarioRepository;
import com.miroma.miroma.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private ParejaService parejaService;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Transactional
    public RegisterResponse registrarUsuario(RegisterRequest request) {
        // Validar que las contraseñas coincidan
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ValidationException("Las contraseñas no coinciden");
        }

        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("El email ya está registrado");
        }

        // Crear nueva entidad Usuario
        Usuario usuario = new Usuario();

        // Combinar nombre y apellido
        String nombreCompleto = request.getNombre().trim() + " " + request.getApellido().trim();
        usuario.setNombre(nombreCompleto);

        // Mapear rol a rol_id (esposo=1, esposa=2)
        if ("esposo".equalsIgnoreCase(request.getRol())) {
            usuario.setRolId(1);
        } else if ("esposa".equalsIgnoreCase(request.getRol())) {
            usuario.setRolId(2);
        } else {
            throw new ValidationException("Rol inválido. Debe ser 'esposo' o 'esposa'");
        }

        // pareja_id se establecerá después de guardar el usuario si se proporciona el email de la pareja
        usuario.setParejaId(null);

        // Establecer email
        usuario.setEmail(request.getEmail().toLowerCase().trim());

        // Hash de contraseña
        String hashedPassword = passwordService.hashPassword(request.getPassword());
        usuario.setPassword(hashedPassword);

        // Convertir LocalDate a java.sql.Date
        usuario.setFechaNacimiento(Date.valueOf(request.getFechaNacimiento()));

        // activo y fecha_creacion se establecen automáticamente en el constructor y @PrePersist
        usuario.setActivo(1);
        usuario.setFechaCreacion(new java.sql.Timestamp(System.currentTimeMillis()));

        // Log para debugging
        logger.info("Intentando guardar usuario: {}", usuario.getEmail());
        logger.info("Datos del usuario - Nombre: {}, Email: {}, Rol: {}", 
                   usuario.getNombre(), usuario.getEmail(), usuario.getRolId());

        // Guardar usuario
        try {
            Usuario usuarioGuardado = usuarioRepository.save(usuario);
            usuarioRepository.flush(); // Forzar el flush para asegurar que se guarde inmediatamente
            
            logger.info("Usuario guardado exitosamente con ID: {}", usuarioGuardado.getId());
            
            // Verificar que realmente se guardó
            Usuario usuarioVerificado = usuarioRepository.findById(usuarioGuardado.getId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado después de guardar"));
            
            logger.info("Usuario verificado en BD - ID: {}, Email: {}, Nombre: {}", 
                       usuarioVerificado.getId(), usuarioVerificado.getEmail(), usuarioVerificado.getNombre());
            
            // Si se proporcionó el email de la pareja, crear la pareja
            if (request.getUsuarioPareja() != null && !request.getUsuarioPareja().trim().isEmpty()) {
                try {
                    Optional<Usuario> parejaOpt = usuarioRepository.findByEmail(request.getUsuarioPareja().toLowerCase().trim());
                    
                    if (parejaOpt.isPresent()) {
                        Usuario pareja = parejaOpt.get();
                        
                        // Verificar que la pareja esté activa
                        if (pareja.getActivo() != null && pareja.getActivo() == 1) {
                            // Verificar que la pareja no tenga ya una pareja vinculada
                            if (pareja.getParejaId() == null) {
                                // Crear la pareja
                                parejaService.crearPareja(usuarioGuardado.getId(), pareja.getId());
                                logger.info("Pareja creada durante el registro: Usuario {} con pareja {}", 
                                          usuarioGuardado.getEmail(), pareja.getEmail());
                            } else {
                                logger.warn("La pareja {} ya tiene una pareja vinculada", pareja.getEmail());
                            }
                        } else {
                            logger.warn("La pareja {} está inactiva", pareja.getEmail());
                        }
                    } else {
                        logger.warn("Pareja con email {} no encontrada durante el registro", request.getUsuarioPareja());
                    }
                } catch (Exception e) {
                    logger.error("Error al crear pareja durante el registro: {}", e.getMessage(), e);
                    // No fallar el registro si hay error al crear la pareja
                }
            }
            
            // Crear respuesta
            RegisterResponse response = new RegisterResponse();
            response.setId(usuarioGuardado.getId());
            response.setNombre(usuarioGuardado.getNombre());
            response.setEmail(usuarioGuardado.getEmail());
            response.setMensaje("Usuario registrado exitosamente");
            response.setFechaCreacion(usuarioGuardado.getFechaCreacion());

            return response;
        } catch (Exception e) {
            logger.error("Error al guardar usuario en la base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        }
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        
        // Verificar si el usuario está bloqueado
        try {
            loginAttemptService.verificarBloqueo(email);
        } catch (ValidationException e) {
            // Si está bloqueado, lanzar excepción con el mensaje apropiado
            throw e;
        }
        
        // Buscar usuario por email
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        
        boolean credencialesInvalidas = false;
        
        if (usuarioOpt.isEmpty()) {
            credencialesInvalidas = true;
        } else {
            Usuario usuario = usuarioOpt.get();

            // Verificar que el usuario esté activo
            if (usuario.getActivo() == null || usuario.getActivo() != 1) {
                credencialesInvalidas = true;
            } else {
                // Verificar contraseña
                if (!passwordService.verifyPassword(request.getPassword(), usuario.getPassword())) {
                    credencialesInvalidas = true;
                }
            }
        }
        
        // Si las credenciales son inválidas, registrar intento fallido
        if (credencialesInvalidas) {
            loginAttemptService.registrarIntentoFallido(email);
            throw new UnauthorizedException("Credenciales inválidas");
        }
        
        // Si llegamos aquí, el login es exitoso
        Usuario usuario = usuarioOpt.get();
        
        // Resetear intentos fallidos después de login exitoso
        loginAttemptService.resetearIntentos(email);

        // Generar access token JWT
        String token = jwtService.generateToken(usuario.getId(), usuario.getEmail(), usuario.getNombre());

        // Generar y guardar refresh token
        com.miroma.miroma.entity.RefreshToken refreshTokenEntity = refreshTokenService.createRefreshToken(usuario.getId());
        String refreshToken = refreshTokenEntity.getToken();

        logger.info("Login exitoso para usuario: {}", usuario.getEmail());

        // Crear respuesta
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRefreshToken(refreshToken);
        response.setId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setMensaje("Login exitoso");
        response.setParejaId(usuario.getParejaId() != null ? usuario.getParejaId().toString() : null);
        response.setHasPartner(usuario.getParejaId() != null);

        return response;
    }

    public CheckUserResponse checkUserExists(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email.toLowerCase().trim());
        
        CheckUserResponse response = new CheckUserResponse();
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            response.setExists(true);
            response.setUserId(usuario.getId());
            response.setNombre(usuario.getNombre());
            response.setEmail(usuario.getEmail());
            response.setMensaje("Usuario encontrado");
        } else {
            response.setExists(false);
            response.setMensaje("Usuario no encontrado");
        }
        
        return response;
    }

    @Transactional
    public LinkPartnerResponse linkPartner(Integer userId, LinkPartnerRequest request) {
        // Obtener el usuario actual
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("usuario", userId));

        // Verificar que el usuario no tenga ya una pareja vinculada
        if (usuario.getParejaId() != null) {
            throw new IllegalArgumentException("Ya tienes una pareja vinculada");
        }

        // Buscar la pareja por email
        Optional<Usuario> parejaOpt = usuarioRepository.findByEmail(request.getPartnerEmail().toLowerCase().trim());
        
        if (parejaOpt.isEmpty()) {
            throw new ResourceNotFoundException("usuario", request.getPartnerEmail());
        }

        Usuario pareja = parejaOpt.get();

        // Verificar que no sea el mismo usuario
        if (usuario.getId().equals(pareja.getId())) {
            throw new IllegalArgumentException("No puedes vincular tu propia cuenta como pareja");
        }

        // Verificar que la pareja esté activa
        if (pareja.getActivo() == null || pareja.getActivo() != 1) {
            throw new IllegalArgumentException("El usuario de la pareja está inactivo");
        }

        // Verificar que la pareja no tenga ya una pareja vinculada
        if (pareja.getParejaId() != null) {
            throw new IllegalArgumentException("El usuario de la pareja ya tiene una pareja vinculada");
        }

        // Crear la pareja (esto actualiza pareja_id en ambos usuarios)
        Pareja parejaCreada = parejaService.crearPareja(usuario.getId(), pareja.getId());

        logger.info("Pareja creada: Usuario {} vinculado con pareja {} (Pareja ID: {})", 
                   usuario.getEmail(), pareja.getEmail(), parejaCreada.getId());

        // Crear respuesta
        LinkPartnerResponse response = new LinkPartnerResponse();
        response.setMensaje("Pareja vinculada exitosamente");
        response.setUserId(usuario.getId());
        response.setPartnerEmail(pareja.getEmail());
        response.setPartnerNombre(pareja.getNombre());

        return response;
    }
}

