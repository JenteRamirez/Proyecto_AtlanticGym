package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Boleta;
import com.gimnasio.demo.model.Plan;
import com.gimnasio.demo.model.Usuario;
import com.gimnasio.demo.payload.PagoRequest;
import com.gimnasio.demo.repository.BoletaRepository;
import com.gimnasio.demo.repository.PlanRepository;
import com.gimnasio.demo.repository.UsuarioRepository;
import com.gimnasio.demo.util.BoletaUtils;
import com.gimnasio.demo.util.PdfGeneratorUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Controlador encargado de procesar pagos de planes de gimnasio por parte de los usuarios.
 *
 * Funciones principales:
 * - Registrar un nuevo pago validando que no haya uno activo.
 * - Generar un comprobante de pago en formato PDF.
 *
 * Dependencias del pom utilizadas:
 * - spring-boot-starter-web → para exponer el endpoint REST.
 * - spring-boot-starter-data-jpa → para operaciones CRUD con Boleta, Usuario y Plan.
 * - itextpdf → para generar el PDF de la boleta de pago.
 *
 * Relaciones:
 * - Usa los repositorios BoletaRepository, PlanRepository y UsuarioRepository.
 * - Utiliza utilidades de negocio como BoletaUtils y PdfGeneratorUtil.
 */

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Registra un pago si el usuario no tiene un plan activo
     * y genera un PDF como comprobante del pago.
     */
    @PostMapping("/pago-pdf")
    public ResponseEntity<?> registrarPagoConPDF(@RequestBody PagoRequest request) {
        // Buscar usuario y plan
        Usuario usuario = usuarioRepository.findById(request.getDocumento()).orElse(null);
        Plan plan = planRepository.findById(request.getIdPlan()).orElse(null);

        if (usuario == null || plan == null) {
            return ResponseEntity.status(404).body("Usuario o plan no encontrado");
        }

        // Verificar si ya tiene un plan activo
        List<Boleta> boletasUsuario = boletaRepository.findByDocumentoUsuario(usuario.getDocumento());
        Date ahora = new Date();

        for (Boleta b : boletasUsuario) {
            Date vencimiento = BoletaUtils.calcularFechaVencimiento(b.getFechaEmision(),
                    b.getPlan().getDuracionMeses());

            if (vencimiento.after(ahora)) {
                return ResponseEntity.badRequest()
                        .body("Ya tienes un plan activo hasta el " + vencimiento);
            }
        }

        // Crear nueva boleta
        Boleta boleta = new Boleta();
        boleta.setDocumentoUsuario(usuario.getDocumento());
        boleta.setPlan(plan);
        boleta.setMontoTotal(BigDecimal.valueOf(plan.getPrecio()));
        boleta.setFechaEmision(ahora);
        boletaRepository.save(boleta);

        // Generar PDF con datos del pago
        byte[] pdfBytes = PdfGeneratorUtil.generarPDFBoleta(boleta);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=boleta.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdfBytes);
    }
}

