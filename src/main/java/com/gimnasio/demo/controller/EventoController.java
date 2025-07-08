package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Evento;
import com.gimnasio.demo.payload.EventoDTO;
import com.gimnasio.demo.repository.EventoAsistenciaRepository;
import com.gimnasio.demo.repository.EventoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador que gestiona la administración de eventos disponibles para los usuarios.
 *
 * Funciones principales:
 * - Listar eventos disponibles con estado de inscripción.
 * - Crear, actualizar y eliminar eventos.
 *
 * Dependencias del pom utilizadas:
 * - spring-boot-starter-web → Para los endpoints REST.
 * - spring-boot-starter-data-jpa → Para persistencia de datos con repositorios.
 *
 * Relaciones:
 * - Utiliza EventoRepository para gestionar entidades Evento.
 * - Utiliza EventoAsistenciaRepository para validar inscripciones a eventos.
 */

@RestController
@RequestMapping("/api/eventos")
public class EventoController {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private EventoAsistenciaRepository asistenciaRepository;

    /**
     * Lista todos los eventos registrados.
     * Si se proporciona un documento (DNI), verifica si el usuario ya está inscrito en cada evento.
     */
    @GetMapping
    public List<EventoDTO> listarEventos(@RequestParam(required = false) String documento) {
        List<Evento> eventos = eventoRepository.findAll();

        return eventos.stream().map(evento -> {
            boolean inscrito = false;

            if (documento != null && !documento.isBlank()) {
                inscrito = asistenciaRepository.existsByIdEventoAndDocumentoUsuario(
                        evento.getIdEvento(), documento);
            }

            return new EventoDTO(
                    evento.getIdEvento(),
                    evento.getTitulo(),
                    evento.getDescripcion(),
                    evento.getFechaEvento().toString(),
                    evento.getCupos(),
                    inscrito);
        }).toList();
    }

    /**
     * Registra un nuevo evento con estado "activo" por defecto.
     */
    @PostMapping
    public ResponseEntity<?> crearEvento(@RequestBody Evento evento) {
        try {
            evento.setEstado("activo"); // por defecto
            Evento nuevo = eventoRepository.save(evento);
            return ResponseEntity.ok(nuevo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al registrar evento: " + e.getMessage());
        }
    }

    /**
     * Actualiza los datos de un evento específico.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarEvento(@PathVariable int id, @RequestBody Evento eventoActualizado) {
        Optional<Evento> optionalEvento = eventoRepository.findById(id);

        if (optionalEvento.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Evento evento = optionalEvento.get();
        evento.setTitulo(eventoActualizado.getTitulo());
        evento.setDescripcion(eventoActualizado.getDescripcion());
        evento.setFechaEvento(eventoActualizado.getFechaEvento());
        evento.setCupos(eventoActualizado.getCupos());
        evento.setEstado(eventoActualizado.getEstado());

        eventoRepository.save(evento);
        return ResponseEntity.ok(evento);
    }

    /**
     * Elimina un evento existente por su ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarEvento(@PathVariable int id) {
        if (!eventoRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        eventoRepository.deleteById(id);
        return ResponseEntity.ok("Evento eliminado correctamente.");
    }
}

