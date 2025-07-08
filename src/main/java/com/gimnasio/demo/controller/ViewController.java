package com.gimnasio.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de vistas encargado de enrutar las URLs hacia las plantillas HTML
 * ubicadas en la carpeta `src/main/resources/templates`.
 *
 * Este controlador se encarga únicamente de redirigir a páginas estáticas o 
 * Thymeleaf sin lógica de backend.
 *
 * Dependencias del pom utilizadas:
 * - spring-boot-starter-thymeleaf → para el procesamiento de vistas HTML.
 * - spring-boot-starter-web → para manejar rutas tipo GET.
 */

@Controller
public class ViewController {

    // 1. Página principal
    @GetMapping("/")
    public String homePage() {
        return "US_Inicio";
    }

    // 2. Autenticación: login y registro
    @GetMapping("/login")
    public String loginPage() {
        return "US_Login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "US_Register";
    }

    // 3. Vista de datos del usuario
    @GetMapping("/datos-usuario")
    public String datosUsuario() {
        return "US_DatosUsuario";
    }

    // 4. Planes y precios
    @GetMapping("/precios")
    public String mostrarPrecios() {
        return "VA_Precios";
    }

    @GetMapping("/planes")
    public String planes() {
        return "US_PlanesYPrecios";
    }

    // 5. Páginas de pago por tipo
    @GetMapping("/pago1")
    public String pagoAPage() {
        return "US_PagoA";
    }

    @GetMapping("/pago2")
    public String pagoBPage() {
        return "US_PagoB";
    }

    @GetMapping("/pago3")
    public String pagoCPage() {
        return "US_PagoC";
    }

    // 6. Suscripciones y resumen de boletas
    @GetMapping("/suscripciones")
    public String vistaSuscripciones() {
        return "VA_Suscripciones";
    }

    @GetMapping("/VistaSuscripciones")
    public String resumenBoletas() {
        return "VA_ResumenBoletas";
    }

    // 7. Panel y administración
    @GetMapping("/admin-panel")
    public String mostrarPanelAdmin() {
        return "VA_Inicio";
    }

    @GetMapping("/TablaUsuarios")
    public String tablaUsuarios() {
        return "tablaUsuarios";
    }

    // 8. Eventos y asistencias
    @GetMapping("/eventos")
    public String eventosUsuario() {
        return "US_Eventos";
    }

    @GetMapping("/eventosAdmin")
    public String eventosAdmin() {
        return "VA_Eventos";
    }

    @GetMapping("/asistencias")
    public String asistencias() {
        return "VA_Asistencias";
    }

    // 9. Anuncios
    @GetMapping("/Anuncios")
    public String anuncios() {
        return "US_Anuncios";
    }
}

