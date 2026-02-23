package com.videogames.service;

import com.videogames.domain.Resena;
import com.videogames.domain.Usuario;
import com.videogames.domain.Videojuego;
import com.videogames.mongo.AuditLogRepository;
import com.videogames.mongo.ResenaMongoRepository;
import com.videogames.repository.ResenaRepository;
import com.videogames.repository.UsuarioRepository;
import com.videogames.repository.VideojuegoRepository;
import org.bson.Document;

import java.util.List;
import java.util.Optional;

public class ResenaService {

    private final ResenaRepository resenaRepo;
    private final VideojuegoRepository videojuegoRepo;
    private final UsuarioRepository usuarioRepo;
    private final AuditLogRepository auditRepo;
    private final ResenaMongoRepository resenaMongoRepo;
    private String usuarioActual;

    public ResenaService(String usuarioActual) {
        this.resenaRepo = new ResenaRepository();
        this.videojuegoRepo = new VideojuegoRepository();
        this.usuarioRepo = new UsuarioRepository();
        this.auditRepo = new AuditLogRepository();
        this.resenaMongoRepo = new ResenaMongoRepository();
        this.usuarioActual = usuarioActual;
    }

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }

    public Resena crear(Long videojuegoId, Long usuarioId, int puntuacion, String comentario) {
        // Validar entidades relacionadas
        Videojuego videojuego = videojuegoRepo.buscarPorId(videojuegoId)
            .orElseThrow(() -> new IllegalArgumentException("Videojuego no encontrado: " + videojuegoId));
        Usuario usuario = usuarioRepo.buscarPorId(usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        if (puntuacion < 1 || puntuacion > 10)
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 10");
        if (comentario == null || comentario.isBlank())
            throw new IllegalArgumentException("El comentario es obligatorio");

        // 1. Persistir en SQL
        Resena resena = new Resena(puntuacion, comentario, videojuego, usuario);
        Resena guardada = resenaRepo.guardar(resena);

        // 2. Guardar snapshot en MongoDB (historial JSON)
        resenaMongoRepo.guardarSnapshot(
            guardada.getId(), videojuego.getId(), videojuego.getTitulo(),
            usuario.getId(), usuario.getUsername(),
            puntuacion, comentario
        );

        // 3. Registrar en audit_logs (MongoDB)
        auditRepo.registrar("CREATE", usuarioActual, "Resena", guardada.getId(),
            new Document()
                .append("videojuego", videojuego.getTitulo())
                .append("usuario", usuario.getUsername())
                .append("puntuacion", puntuacion)
        );

        return guardada;
    }

    public boolean eliminar(Long id) {
        Optional<Resena> r = resenaRepo.buscarPorId(id);
        if (r.isEmpty()) return false;

        boolean eliminado = resenaRepo.eliminar(id);
        if (eliminado) {
            auditRepo.registrar("DELETE", usuarioActual, "Resena", id,
                new Document().append("puntuacion", r.get().getPuntuacion())
            );
        }
        return eliminado;
    }

    public List<Resena> listarPorVideojuego(Long videojuegoId) {
        return resenaRepo.listarPorVideojuego(videojuegoId);
    }

    public List<Resena> listarPorUsuario(Long usuarioId) {
        return resenaRepo.listarPorUsuario(usuarioId);
    }

    // Consultar historial MongoDB de reseñas por juego
    public List<Document> historialResenasPorJuego(Long videojuegoId) {
        return resenaMongoRepo.buscarPorVideojuego(videojuegoId);
    }

    // Estadísticas agregación MongoDB
    public List<Document> estadisticasResenas() {
        return resenaMongoRepo.estadisticasPorJuego();
    }
}
