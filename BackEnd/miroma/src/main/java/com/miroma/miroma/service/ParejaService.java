package com.miroma.miroma.service;

import com.miroma.miroma.entity.Pareja;
import com.miroma.miroma.entity.Usuario;
import com.miroma.miroma.repository.ParejaRepository;
import com.miroma.miroma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ParejaService {

    @Autowired
    private ParejaRepository parejaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Crea una pareja entre dos usuarios
     * @param usuario1Id ID del primer usuario
     * @param usuario2Id ID del segundo usuario
     * @return La pareja creada
     */
    @Transactional
    public Pareja crearPareja(Integer usuario1Id, Integer usuario2Id) {
        // Obtener ambos usuarios
        Usuario usuario1 = usuarioRepository.findById(usuario1Id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario 1 no encontrado"));
        
        Usuario usuario2 = usuarioRepository.findById(usuario2Id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario 2 no encontrado"));

        // Determinar quién es esposo y quién es esposa
        Integer esposoId, esposaId;
        if (usuario1.getRolId() == 1) { // usuario1 es esposo
            esposoId = usuario1.getId();
            esposaId = usuario2.getId();
        } else { // usuario1 es esposa
            esposoId = usuario2.getId();
            esposaId = usuario1.getId();
        }

        // Crear nombre de la pareja (unión de ambos nombres)
        String nombrePareja = usuario1.getNombre() + " & " + usuario2.getNombre();

        // Crear la pareja
        Pareja pareja = new Pareja(nombrePareja, esposoId, esposaId);
        pareja = parejaRepository.save(pareja);
        parejaRepository.flush();

        // Actualizar pareja_id en ambos usuarios
        usuario1.setParejaId(pareja.getId());
        usuario2.setParejaId(pareja.getId());
        
        usuarioRepository.save(usuario1);
        usuarioRepository.save(usuario2);
        usuarioRepository.flush();

        return pareja;
    }

    /**
     * Verifica si un usuario ya tiene una pareja
     * @param userId ID del usuario
     * @return true si tiene pareja, false en caso contrario
     */
    public boolean tienePareja(Integer userId) {
        Optional<Pareja> pareja = parejaRepository.findByUsuarioId(userId);
        return pareja.isPresent();
    }

    /**
     * Obtiene la pareja de un usuario
     * @param userId ID del usuario
     * @return La pareja si existe
     */
    public Optional<Pareja> obtenerPareja(Integer userId) {
        return parejaRepository.findByUsuarioId(userId);
    }
}

