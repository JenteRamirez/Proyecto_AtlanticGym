package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Plan;
import com.gimnasio.demo.repository.PlanRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador encargado de la gestión de planes de membresía del gimnasio.
 *
 * Funciones principales:
 * - Listar todos los planes disponibles.
 * - Consultar, actualizar y eliminar planes por ID.
 *
 * Dependencias del pom utilizadas:
 * - spring-boot-starter-web → para crear endpoints REST.
 * - spring-boot-starter-data-jpa → para operaciones con la base de datos (PlanRepository).
 *
 * Relaciones:
 * - Utiliza PlanRepository para acceder a las entidades Plan.
 */

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/planes")
public class PlanController {

    @Autowired
    private PlanRepository planRepository;

    /**
     * Actualiza un plan existente dado su ID.
     * Solo se realiza si el plan existe.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPlan(@PathVariable int id, @RequestBody Plan planActualizado) {
        Optional<Plan> planOpt = planRepository.findById(id);
        if (planOpt.isPresent()) {
            Plan plan = planOpt.get();
            plan.setNombre(planActualizado.getNombre());
            plan.setDuracionMeses(planActualizado.getDuracionMeses());
            plan.setPrecio(planActualizado.getPrecio());
            plan.setDescripcion(planActualizado.getDescripcion());

            planRepository.save(plan);
            return ResponseEntity.ok("Plan actualizado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan no encontrado.");
        }
    }

    /**
     * Elimina un plan si existe en base de datos.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPlan(@PathVariable int id) {
        if (planRepository.existsById(id)) {
            planRepository.deleteById(id);
            return ResponseEntity.ok("Plan eliminado correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan no encontrado.");
        }
    }

    /**
     * Lista todos los planes de membresía disponibles.
     */
    @GetMapping
    public ResponseEntity<?> listarPlanes() {
        return ResponseEntity.ok(planRepository.findAll());
    }

    /**
     * Devuelve un plan específico por su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Plan> obtenerPlanPorId(@PathVariable int id) {
        return planRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

