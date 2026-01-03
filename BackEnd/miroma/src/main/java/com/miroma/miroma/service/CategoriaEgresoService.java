package com.miroma.miroma.service;

import com.miroma.miroma.dto.CategoriaEgresoRequest;
import com.miroma.miroma.dto.CategoriaEgresoResponse;
import com.miroma.miroma.entity.CategoriaEgreso;
import com.miroma.miroma.repository.CategoriaEgresoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaEgresoService {

    @Autowired
    private CategoriaEgresoRepository categoriaEgresoRepository;

    @Autowired
    private LogEventoService logEventoService;

    @Transactional
    public CategoriaEgresoResponse crearCategoria(Integer userId, CategoriaEgresoRequest request) {
        // Verificar si ya existe una categoría con el mismo nombre (case-insensitive)
        if (categoriaEgresoRepository.existsByNombreIgnoreCase(request.getNombre().trim())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        CategoriaEgreso categoria = new CategoriaEgreso();
        categoria.setNombre(request.getNombre().trim());
        categoria.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);

        categoria = categoriaEgresoRepository.save(categoria);
        categoriaEgresoRepository.flush();

        return mapToResponse(categoria);
    }

    public List<CategoriaEgresoResponse> obtenerTodasLasCategorias() {
        List<CategoriaEgreso> categorias = categoriaEgresoRepository.findAllByOrderByNombreAsc();
        return categorias.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public CategoriaEgresoResponse obtenerCategoriaPorId(Integer id) {
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        return mapToResponse(categoria);
    }

    @Transactional
    public CategoriaEgresoResponse actualizarCategoria(Integer id, Integer userId, CategoriaEgresoRequest request) {
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Verificar si el nuevo nombre ya existe en otra categoría
        String nuevoNombre = request.getNombre().trim();
        if (!categoria.getNombre().equalsIgnoreCase(nuevoNombre)) {
            if (categoriaEgresoRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + nuevoNombre);
            }
        }

        categoria.setNombre(nuevoNombre);
        categoria.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);

        categoria = categoriaEgresoRepository.save(categoria);
        categoriaEgresoRepository.flush();

        // Registrar evento en log
        logEventoService.registrarEvento(userId, 
            String.format("Actualizó la categoría de egreso (ID: %d) - Nuevo nombre: '%s'", id, categoria.getNombre()));

        return mapToResponse(categoria);
    }

    @Transactional
    public void eliminarCategoria(Integer id, Integer userId) {
        CategoriaEgreso categoria = categoriaEgresoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        // Registrar evento en log antes de eliminar
        logEventoService.registrarEvento(userId, 
            String.format("Eliminó la categoría de egreso (ID: %d) - Nombre: '%s'", id, categoria.getNombre()));

        categoriaEgresoRepository.delete(categoria);
    }

    private CategoriaEgresoResponse mapToResponse(CategoriaEgreso categoria) {
        CategoriaEgresoResponse response = new CategoriaEgresoResponse();
        response.setId(categoria.getId());
        response.setNombre(categoria.getNombre());
        response.setDescripcion(categoria.getDescripcion());
        return response;
    }
}

