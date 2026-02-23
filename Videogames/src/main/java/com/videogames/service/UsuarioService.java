package com.videogames.service;

import com.videogames.domain.Usuario;
import com.videogames.mongo.AuditLogRepository;
import com.videogames.repository.UsuarioRepository;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

public class UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final AuditLogRepository auditRepo;
    private String usuarioActual;

    public UsuarioService(String usuarioActual) {
        this.usuarioRepo = new UsuarioRepository();
        this.auditRepo = new AuditLogRepository();
        this.usuarioActual = usuarioActual;
    }

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }

    public Usuario crear(Usuario usuario) {
        validar(usuario);
        // Verificar username único
        if (usuarioRepo.buscarPorUsername(usuario.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con username: " + usuario.getUsername());
        }
        Usuario guardado = usuarioRepo.guardar(usuario);

        auditRepo.registrar("CREATE", usuarioActual, "Usuario", guardado.getId(),
            new Document().append("username", guardado.getUsername()).append("rol", guardado.getRol().name())
        );

        return guardado;
    }

    public Usuario actualizar(Usuario usuario) {
        if (usuario.getId() == null) throw new IllegalArgumentException("El usuario debe tener ID para actualizar");
        validar(usuario);
        Usuario actualizado = usuarioRepo.guardar(usuario);

        auditRepo.registrar("UPDATE", usuarioActual, "Usuario", actualizado.getId(),
            new Document().append("username", actualizado.getUsername())
        );

        return actualizado;
    }

    public boolean eliminar(Long id) {
        Optional<Usuario> u = usuarioRepo.buscarPorId(id);
        if (u.isEmpty()) return false;

        boolean eliminado = usuarioRepo.eliminar(id);
        if (eliminado) {
            auditRepo.registrar("DELETE", usuarioActual, "Usuario", id,
                new Document().append("username", u.get().getUsername())
            );
        }
        return eliminado;
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepo.buscarPorId(id);
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepo.buscarPorUsername(username);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepo.listarTodos();
    }

    private void validar(Usuario u) {
        if (u.getUsername() == null || u.getUsername().isBlank())
            throw new IllegalArgumentException("El username es obligatorio");
        if (u.getEmail() == null || u.getEmail().isBlank())
            throw new IllegalArgumentException("El email es obligatorio");
        if (!u.getEmail().contains("@"))
            throw new IllegalArgumentException("El email no es válido");
        if (u.getNombre() == null || u.getNombre().isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
    }
}
