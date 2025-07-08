package com.gimnasio.demo.controller;

import com.gimnasio.demo.model.Usuario;
import com.gimnasio.demo.model.TipoUsuario;
import com.gimnasio.demo.payload.LoginRequest;
import com.gimnasio.demo.payload.RegisterRequest;
import com.gimnasio.demo.repository.UsuarioRepository;
import com.gimnasio.demo.repository.TipoUsuarioRepository;
import com.gimnasio.demo.validator.DocumentoValidator;

import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Controlador encargado de gestionar la autenticación y administración de usuarios.
 * 
 * Funciones principales:
 * - Registro de nuevos usuarios con validación de DNI y encriptación de contraseñas.
 * - Autenticación (login) de usuarios con verificación de credenciales.
 * - CRUD completo de usuarios (crear, consultar, actualizar, eliminar).
 * 
 * Tecnologías relacionadas:
 * - Spring Web (para exponer los endpoints REST).
 * - Spring Security Crypto (para encriptar contraseñas con BCrypt).
 * - Spring Data JPA (para acceder y manipular los datos de usuario).
 * - Commons Validator (para validar que el DNI tenga 8 dígitos).
 */

@CrossOrigin(origins = "*")
@RestController
public class AuthController {

    private final UsuarioRepository repo;
    private final TipoUsuarioRepository tipoRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    // Constructor que inyecta dependencias necesarias para el controlador
    public AuthController(UsuarioRepository repo, TipoUsuarioRepository tipoRepo) {
        this.repo = repo;
        this.tipoRepo = tipoRepo;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Registro de usuario
     * Valida que el DNI tenga 8 dígitos y que el usuario no exista.
     * Encripta la contraseña antes de guardarla.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (!DocumentoValidator.esDNIValido(req.getDocumento())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("El documento debe contener exactamente 8 dígitos numéricos.");
        }

        if (repo.existsById(req.getDocumento())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Ya existe un usuario con ese documento");
        }

        Optional<TipoUsuario> tipoOpt = tipoRepo.findById(req.getIdTipo());
        if (tipoOpt.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Tipo de usuario inválido");
        }

        Usuario nuevo = new Usuario();
        nuevo.setDocumento(req.getDocumento());
        nuevo.setNombres(req.getNombres());
        nuevo.setApellidos(req.getApellidos());
        nuevo.setTelefono(req.getTelefono());
        nuevo.setTipoUsuario(tipoOpt.get());
        nuevo.setPassword(passwordEncoder.encode(req.getPassword()));

        repo.save(nuevo);
        return ResponseEntity.ok("Usuario registrado exitosamente.");
    }

    /**
     * Login básico de usuario
     * Compara la contraseña ingresada con la almacenada encriptada
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> usrOpt = repo.findById(req.getDocumento());

        if (usrOpt.isPresent()) {
            Usuario usr = usrOpt.get();
            if (passwordEncoder.matches(req.getPassword(), usr.getPassword())) {
                Map<String, String> respuesta = new HashMap<>();
                respuesta.put("dni", usr.getDocumento());
                respuesta.put("rango", usr.getTipoUsuario().getDescripcion()); // admin o usuario
                return ResponseEntity.ok(respuesta);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas.");
    }

    /**
     * Consulta de usuario por DNI
     */
    @GetMapping("/api/usuarios/{dni}")
    public ResponseEntity<?> getUsuarioByDni(@PathVariable String dni) {
        Optional<Usuario> usrOpt = repo.findById(dni);

        if (usrOpt.isPresent()) {
            Usuario usr = usrOpt.get();
            Map<String, Object> userData = new HashMap<>();
            userData.put("documento", usr.getDocumento());
            userData.put("nombres", usr.getNombres());
            userData.put("apellidos", usr.getApellidos());
            userData.put("telefono", usr.getTelefono());
            userData.put("idTipo", usr.getTipoUsuario().getId());
            userData.put("tipoDescripcion", usr.getTipoUsuario().getDescripcion());

            return ResponseEntity.ok(userData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
    }

    /**
     * Eliminación de usuario por DNI
     */
    @DeleteMapping("/api/usuarios/{dni}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable String dni) {
        if (repo.existsById(dni)) {
            repo.deleteById(dni);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado");
        }
    }

    /**
     * Listado general de usuarios (para tabla de usuarios)
     */
    @GetMapping("/api/usuarios")
    public List<Map<String, Object>> listarUsuarios() {
        List<Usuario> usuarios = repo.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Usuario u : usuarios) {
            Map<String, Object> map = new HashMap<>();
            map.put("documento", u.getDocumento());
            map.put("nombres", u.getNombres());
            map.put("apellidos", u.getApellidos());
            map.put("telefono", u.getTelefono());
            map.put("rango", u.getTipoUsuario().getDescripcion()); // se muestra descripción, no ID
            result.add(map);
        }

        return result;
    }

    /**
     * Actualización de usuario
     * Permite modificar nombres, apellidos, teléfono, contraseña y tipo
     */
    @PutMapping("/api/usuarios")
    public ResponseEntity<?> updateUsuario(@RequestBody Usuario updateRequest) {
        Optional<Usuario> usrOpt = repo.findById(updateRequest.getDocumento());

        if (usrOpt.isPresent()) {
            Usuario existingUser = usrOpt.get();

            if (updateRequest.getNombres() != null) {
                existingUser.setNombres(updateRequest.getNombres());
            }
            if (updateRequest.getApellidos() != null) {
                existingUser.setApellidos(updateRequest.getApellidos());
            }
            if (updateRequest.getTelefono() != null) {
                existingUser.setTelefono(updateRequest.getTelefono());
            }
            if (updateRequest.getPassword() != null && !updateRequest.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updateRequest.getPassword()));
            }

            if (updateRequest.getTipoUsuario() != null) {
                Optional<TipoUsuario> tipoOpt = tipoRepo.findById(updateRequest.getTipoUsuario().getId());
                tipoOpt.ifPresent(existingUser::setTipoUsuario);
            }

            repo.save(existingUser);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado para actualizar.");
        }
    }
}

