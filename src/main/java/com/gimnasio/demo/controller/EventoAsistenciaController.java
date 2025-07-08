package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Evento;
import com.gimnasio.demo.model.EventoAsistencia;
import com.gimnasio.demo.model.Usuario;
import com.gimnasio.demo.repository.EventoAsistenciaRepository;
import com.gimnasio.demo.repository.EventoRepository;
import com.gimnasio.demo.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador encargado de gestionar las inscripciones de usuarios a eventos.
 *
 * Funciones principales:
 * - Registrar una asistencia a un evento si hay cupo disponible.
 * - Listar todas las asistencias con detalle de usuario y evento.
 * - Eliminar inscripciones, liberando el cupo correspondiente.
 *
 * Dependencias usadas del `pom.xml`:
 * - `spring-boot-starter-web` → para endpoints REST.
 * - `spring-boot-starter-data-jpa` → acceso a datos con repositorios.
 *
 * Observación sobre duplicidad:
 * - No presenta métodos redundantes con `AuthController` o `BoletaController`.
 * - La obtención del nombre de usuario y datos de eventos sí se repite lógicamente (puede unificarse en servicios más adelante).
 */


@RestController
@RequestMapping("/api/asistencias")
public class EventoAsistenciaController {

    @Autowired
    private EventoAsistenciaRepository asistenciaRepo;

    @Autowired
    private EventoRepository eventoRepo;

    @Autowired
    private UsuarioRepository usuarioRepo;

    /**
     * Registra una asistencia a un evento específico.
     * Requiere:
     *  - Que el evento exista.
     *  - Que haya cupos disponibles.
     *  - Que el usuario no esté ya inscrito.
     */
    @PostMapping
    public ResponseEntity<?> registrarAsistencia(@RequestParam int idEvento, @RequestParam String documento) {
        try {
            Optional<Evento> optionalEvento = eventoRepo.findById(idEvento);
            if (optionalEvento.isEmpty()) {
                return ResponseEntity.badRequest().body("Evento no encontrado");
            }

            Evento evento = optionalEvento.get();

            if (evento.getCupos() <= 0) {
                return ResponseEntity.badRequest().body("Ya no hay cupos disponibles.");
            }

            if (asistenciaRepo.existsByIdEventoAndDocumentoUsuario(idEvento, documento)) {
                return ResponseEntity.badRequest().body("Ya estás inscrito en este evento.");
            }

            // Registro de asistencia
            EventoAsistencia asistencia = new EventoAsistencia();
            asistencia.setIdEvento(idEvento);
            asistencia.setDocumentoUsuario(documento);
            asistenciaRepo.save(asistencia);

            // Reducir cupo en evento
            evento.setCupos(evento.getCupos() - 1);
            eventoRepo.save(evento);

            return ResponseEntity.ok("Inscripción exitosa");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al inscribirse: " + e.getMessage());
        }
    }

    /**
     * Lista todas las asistencias con detalles del evento y del usuario.
     */
    @GetMapping
    public List<Map<String, Object>> listarAsistencias() {
        List<EventoAsistencia> asistencias = asistenciaRepo.findAll();
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (EventoAsistencia a : asistencias) {
            Map<String, Object> fila = new HashMap<>();

            Optional<Evento> ev = eventoRepo.findById(a.getIdEvento());
            Optional<Usuario> us = usuarioRepo.findById(a.getDocumentoUsuario());

            fila.put("idEvento", a.getIdEvento());
            fila.put("documentoUsuario", a.getDocumentoUsuario());
            fila.put("fechaInscripcion", a.getFechaInscripcion());

            fila.put("tituloEvento", ev.map(Evento::getTitulo).orElse("Desconocido"));
            fila.put("nombreUsuario", us.map(u -> u.getNombres() + " " + u.getApellidos()).orElse("Desconocido"));

            resultado.add(fila);
        }

        return resultado;
    }

    /**
     * Elimina una inscripción a un evento según ID y documento.
     * También libera el cupo del evento correspondiente.
     */
    @DeleteMapping("/{idEvento}/{documento}")
    public ResponseEntity<?> eliminarInscripcion(@PathVariable int idEvento, @PathVariable String documento) {
        try {
            Optional<EventoAsistencia> asistenciaOpt = asistenciaRepo.findByIdEventoAndDocumentoUsuario(idEvento, documento);

            if (asistenciaOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Inscripción no encontrada.");
            }

            asistenciaRepo.delete(asistenciaOpt.get());

            // Liberar cupo
            eventoRepo.findById(idEvento).ifPresent(ev -> {
                ev.setCupos(ev.getCupos() + 1);
                eventoRepo.save(ev);
            });

            return ResponseEntity.ok("Inscripción eliminada correctamente.");

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al eliminar inscripción: " + e.getMessage());
        }
    }
}



