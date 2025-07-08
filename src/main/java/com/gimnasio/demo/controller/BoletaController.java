package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Boleta;
import com.gimnasio.demo.repository.BoletaRepository;
import com.gimnasio.demo.util.BoletaUtils;
import com.gimnasio.demo.payload.ResumenBoletaDTO;

import com.google.common.collect.ListMultimap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;

/**
 * Controlador responsable de gestionar operaciones relacionadas con las boletas de pago.
 *
 * Funciones principales:
 * - Listar todas las boletas existentes.
 * - Eliminar boletas por ID.
 * - Agrupar boletas por usuario usando Google Guava.
 * - Generar resumen de pagos por usuario.
 * - Consultar boletas de un usuario específico.
 * - Determinar el plan activo actual de un usuario.
 *
 * Tecnologías utilizadas:
 * - Spring Web (exposición de endpoints REST).
 * - Spring Data JPA (`BoletaRepository`).
 * - Google Guava (`ListMultimap`) para agrupamiento eficiente.
 * - Java Time API (`LocalDate`, `ZoneId`) para cálculos de fechas.
 *
 * Dependencias usadas del `pom.xml`:
 * - `spring-boot-starter-web`
 * - `spring-boot-starter-data-jpa`
 * - `guava`
 */

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class BoletaController {

    @Autowired
    private BoletaRepository boletaRepository;

    /**
     * Devuelve todas las boletas registradas en el sistema.
     */
    @GetMapping("/boletas")
    public List<Boleta> listarBoletas() {
        return boletaRepository.findAll();
    }

    /**
     * Elimina una boleta específica por su ID.
     */
    @DeleteMapping("/boletas/{id}")
    public ResponseEntity<?> eliminarBoleta(@PathVariable int id) {
        if (!boletaRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Boleta no encontrada");
        }
        boletaRepository.deleteById(id);
        return ResponseEntity.ok("Boleta eliminada");
    }

    /**
     * Agrupa todas las boletas por DNI del usuario.
     * Utiliza una utilidad basada en Google Guava para generar la agrupación.
     */
    @GetMapping("/boletas/agrupadas")
    public ListMultimap<String, Boleta> boletasAgrupadasPorDni() {
        List<Boleta> boletas = boletaRepository.findAll();
        return BoletaUtils.agruparPorUsuario(boletas);
    }

    /**
     * Devuelve un resumen de boletas por usuario.
     * Muestra el total pagado y la cantidad de boletas emitidas por cada usuario.
     */
    @GetMapping("/boletas/agrupadas/resumen")
    public List<ResumenBoletaDTO> resumenPorUsuario() {
        List<Boleta> boletas = boletaRepository.findAll();
        ListMultimap<String, Boleta> agrupadas = BoletaUtils.agruparPorUsuario(boletas);

        List<ResumenBoletaDTO> resumen = new ArrayList<>();

        for (String documento : agrupadas.keySet()) {
            List<Boleta> boletasUsuario = agrupadas.get(documento);

            BigDecimal total = boletasUsuario.stream()
                    .map(Boleta::getMontoTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            resumen.add(new ResumenBoletaDTO(
                    documento,
                    total,
                    boletasUsuario.size()));
        }

        return resumen;
    }

    /**
     * Devuelve todas las boletas emitidas a un usuario según su documento (DNI).
     */
    @GetMapping("/boletas/{documento}")
    public List<Boleta> obtenerBoletasPorUsuario(@PathVariable String documento) {
        return boletaRepository.findByDocumentoUsuario(documento);
    }

    /**
     * Determina el plan actual activo del usuario según la fecha actual.
     * Compara la fecha de emisión con la duración del plan para saber si sigue vigente.
     */
    @GetMapping("/usuarios/{dni}/plan-actual")
    public ResponseEntity<?> obtenerPlanActual(@PathVariable String dni) {
        List<Boleta> boletas = boletaRepository.findByDocumentoUsuario(dni);
        LocalDate hoy = LocalDate.now();

        for (Boleta boleta : boletas) {
            LocalDate inicio = boleta.getFechaEmision().toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate fin = inicio.plusMonths(boleta.getPlan().getDuracionMeses());

            if (!hoy.isBefore(inicio) && !hoy.isAfter(fin)) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("idBoleta", boleta.getIdBoleta());
                resp.put("nombrePlan", boleta.getPlan().getNombre());
                resp.put("fechaInicio", inicio.toString());
                resp.put("fechaFin", fin.toString());
                return ResponseEntity.ok(resp);
            }
        }
        return ResponseEntity.status(404).body("No hay plan activo");
    }
}

