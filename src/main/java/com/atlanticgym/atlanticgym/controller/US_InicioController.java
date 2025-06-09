package com.atlanticgym.atlanticgym.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class US_InicioController {

    @GetMapping("/")
    public String mostrarInicio() {
        return "US_Inicio";
    }

    @GetMapping("/USanuncios")
    public String mostrarAnuncios() {
        return "US_Anuncios";
    }

    @GetMapping("/USplanesYprecios")
    public String mostrarplanesYprecios() {
        return "US_PlanesYPrecios";
    }

    @GetMapping("/USlogin")
    public String mostrarlogin() {
        return "US_login";
    }

    @GetMapping("/USregistro")
    public String mostrarregistro() {
        return "US_Register";
    }

    @GetMapping("/USpagoA")
    public String mostrarpagoA() {
        return "US_PagoA";
    }

    @GetMapping("/USpagoB")
    public String mostrarpagoB() {
        return "US_PagoB";
    }

    @GetMapping("/USpagoC")
    public String mostrarpagoC() {
        return "US_PagoC";
    }

    @GetMapping("/VAinicio")
    public String mostrarvainicio() {
        return "VA_Inicio";
    }

    @GetMapping("/VAprecios")
    public String mostrarvaprecios() {
        return "VA_Precios";
    }

    @GetMapping("/VAusuarios")
    public String mostrarvausuarios() {
        return "VA_Usuarios";
    }
}
