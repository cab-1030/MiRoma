package com.miroma.miroma.service;

import com.miroma.miroma.entity.IntentoLogin;
import com.miroma.miroma.exception.ValidationException;
import com.miroma.miroma.repository.IntentoLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Optional;

@Service
public class LoginAttemptService {

    private static final int MAX_INTENTOS = 3;
    private static final int TIEMPO_BLOQUEO_INICIAL_MINUTOS = 3;

    @Autowired
    private IntentoLoginRepository intentoLoginRepository;

    /**
     * Verifica si el email está bloqueado
     * @param email Email del usuario
     * @throws ValidationException Si el usuario está bloqueado
     */
    public void verificarBloqueo(String email) {
        Optional<IntentoLogin> intentoOpt = intentoLoginRepository.findByEmail(email.toLowerCase().trim());
        
        if (intentoOpt.isPresent()) {
            IntentoLogin intento = intentoOpt.get();
            
            // Verificar si está bloqueado
            if (intento.getBloqueadoHasta() != null) {
                Timestamp ahora = new Timestamp(System.currentTimeMillis());
                
                if (intento.getBloqueadoHasta().after(ahora)) {
                    // Aún está bloqueado
                    long minutosRestantes = calcularMinutosRestantes(intento.getBloqueadoHasta(), ahora);
                    throw new ValidationException(
                        String.format("Tu cuenta está temporalmente bloqueada debido a múltiples intentos fallidos. " +
                                     "Por favor, intenta nuevamente en %d minuto(s).", minutosRestantes)
                    );
                } else {
                    // El bloqueo expiró, resetear intentos pero mantener nivel de bloqueo
                    intento.setBloqueadoHasta(null);
                    intento.setIntentosFallidos(0);
                    intentoLoginRepository.save(intento);
                }
            }
        }
    }

    /**
     * Registra un intento fallido de login
     * @param email Email del usuario
     */
    @Transactional
    public void registrarIntentoFallido(String email) {
        String emailNormalizado = email.toLowerCase().trim();
        Optional<IntentoLogin> intentoOpt = intentoLoginRepository.findByEmail(emailNormalizado);
        
        IntentoLogin intento;
        if (intentoOpt.isPresent()) {
            intento = intentoOpt.get();
        } else {
            intento = new IntentoLogin(emailNormalizado);
        }
        
        // Incrementar intentos fallidos
        intento.setIntentosFallidos(intento.getIntentosFallidos() + 1);
        intento.setUltimoIntento(new Timestamp(System.currentTimeMillis()));
        
        // Si alcanzó el máximo de intentos, bloquear
        if (intento.getIntentosFallidos() >= MAX_INTENTOS) {
            bloquearUsuario(intento);
        }
        
        intentoLoginRepository.save(intento);
    }

    /**
     * Bloquea al usuario por un tiempo determinado según su nivel de bloqueo
     * @param intento Objeto IntentoLogin
     */
    private void bloquearUsuario(IntentoLogin intento) {
        // Calcular tiempo de bloqueo: 3 minutos * 2^nivelBloqueo
        int minutosBloqueo = TIEMPO_BLOQUEO_INICIAL_MINUTOS * (int) Math.pow(2, intento.getNivelBloqueo());
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutosBloqueo);
        Timestamp bloqueadoHasta = new Timestamp(cal.getTimeInMillis());
        
        intento.setBloqueadoHasta(bloqueadoHasta);
        intento.setIntentosFallidos(0); // Resetear contador para el próximo ciclo
        intento.setNivelBloqueo(intento.getNivelBloqueo() + 1); // Incrementar nivel de bloqueo
    }

    /**
     * Resetea los intentos fallidos después de un login exitoso
     * @param email Email del usuario
     */
    @Transactional
    public void resetearIntentos(String email) {
        Optional<IntentoLogin> intentoOpt = intentoLoginRepository.findByEmail(email.toLowerCase().trim());
        
        if (intentoOpt.isPresent()) {
            IntentoLogin intento = intentoOpt.get();
            intento.setIntentosFallidos(0);
            intento.setUltimoIntento(new Timestamp(System.currentTimeMillis()));
            intento.setBloqueadoHasta(null);
            // No resetear el nivel de bloqueo, se mantiene para futuros bloqueos
            intentoLoginRepository.save(intento);
        }
    }

    /**
     * Calcula los minutos restantes de bloqueo
     * @param bloqueadoHasta Timestamp de cuando expira el bloqueo
     * @param ahora Timestamp actual
     * @return Minutos restantes (redondeado hacia arriba)
     */
    private long calcularMinutosRestantes(Timestamp bloqueadoHasta, Timestamp ahora) {
        long diferenciaMillis = bloqueadoHasta.getTime() - ahora.getTime();
        long minutos = diferenciaMillis / (60 * 1000);
        long segundosRestantes = (diferenciaMillis % (60 * 1000)) / 1000;
        
        // Redondear hacia arriba si hay segundos restantes
        return segundosRestantes > 0 ? minutos + 1 : minutos;
    }

    /**
     * Obtiene información sobre el estado de bloqueo (para debugging)
     * @param email Email del usuario
     * @return Información del bloqueo o null si no existe
     */
    public String obtenerInfoBloqueo(String email) {
        Optional<IntentoLogin> intentoOpt = intentoLoginRepository.findByEmail(email.toLowerCase().trim());
        
        if (intentoOpt.isPresent()) {
            IntentoLogin intento = intentoOpt.get();
            if (intento.getBloqueadoHasta() != null) {
                Timestamp ahora = new Timestamp(System.currentTimeMillis());
                if (intento.getBloqueadoHasta().after(ahora)) {
                    long minutos = calcularMinutosRestantes(intento.getBloqueadoHasta(), ahora);
                    return String.format("Bloqueado por %d minutos. Nivel: %d", minutos, intento.getNivelBloqueo());
                }
            }
            return String.format("Intentos fallidos: %d/%d. Nivel bloqueo: %d", 
                intento.getIntentosFallidos(), MAX_INTENTOS, intento.getNivelBloqueo());
        }
        
        return null;
    }
}

