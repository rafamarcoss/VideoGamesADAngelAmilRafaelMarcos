package com.videogames.service;

import com.videogames.domain.Videojuego;
import com.videogames.domain.VideojuegoDTO;
import com.videogames.mongo.AuditLogRepository;
import com.videogames.repository.VideojuegoRepository;
import org.bson.Document;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VideojuegoService {

    private final VideojuegoRepository videojuegoRepo;
    private final AuditLogRepository auditRepo;
    private String usuarioActual;

    public VideojuegoService(String usuarioActual) {
        this.videojuegoRepo = new VideojuegoRepository();
        this.auditRepo = new AuditLogRepository();
        this.usuarioActual = usuarioActual;
    }

    public void setUsuarioActual(String usuario) {
        this.usuarioActual = usuario;
    }

    // ===== CRUD CON INTEGRACIÓN MONGO (AUDITORÍA) =====

    public Videojuego crear(Videojuego videojuego) {
        validar(videojuego);
        Videojuego guardado = videojuegoRepo.guardar(videojuego);

        // *** INTEGRACIÓN SQL → MONGO ***
        // Al crear en SQL, se registra evento en MongoDB
        auditRepo.registrar("CREATE", usuarioActual, "Videojuego", guardado.getId(),
            new Document()
                .append("titulo", guardado.getTitulo())
                .append("genero", guardado.getGenero())
                .append("desarrollador", guardado.getDesarrollador())
                .append("precio", guardado.getPrecio())
        );

        return guardado;
    }

    public Videojuego actualizar(Videojuego videojuego) {
        if (videojuego.getId() == null) throw new IllegalArgumentException("El videojuego debe tener ID para actualizar");
        validar(videojuego);

        // Obtener estado anterior para el log
        Optional<Videojuego> anterior = videojuegoRepo.buscarPorId(videojuego.getId());

        Videojuego actualizado = videojuegoRepo.guardar(videojuego);

        // *** INTEGRACIÓN SQL → MONGO ***
        Document payload = new Document()
            .append("titulo", actualizado.getTitulo())
            .append("precioNuevo", actualizado.getPrecio());
        anterior.ifPresent(a -> payload.append("precioAnterior", a.getPrecio()));

        auditRepo.registrar("UPDATE", usuarioActual, "Videojuego", actualizado.getId(), payload);

        return actualizado;
    }

    public boolean eliminar(Long id) {
        Optional<Videojuego> v = videojuegoRepo.buscarPorId(id);
        if (v.isEmpty()) return false;

        boolean eliminado = videojuegoRepo.eliminar(id);
        if (eliminado) {
            // *** INTEGRACIÓN SQL → MONGO ***
            auditRepo.registrar("DELETE", usuarioActual, "Videojuego", id,
                new Document().append("titulo", v.get().getTitulo())
            );
        }
        return eliminado;
    }

    public Optional<Videojuego> buscarPorId(Long id) {
        Optional<Videojuego> result = videojuegoRepo.buscarPorId(id);
        result.ifPresent(v ->
            auditRepo.registrar("READ", usuarioActual, "Videojuego", id,
                new Document().append("titulo", v.getTitulo()))
        );
        return result;
    }

    public List<Videojuego> listarTodos() {
        return videojuegoRepo.listarTodos();
    }

    public List<Videojuego> buscarPorTitulo(String titulo) {
        return videojuegoRepo.buscarPorTitulo(titulo);
    }

    // ===== CONSULTAS AVANZADAS =====

    public List<Videojuego> buscarPorGeneroYPrecio(String genero, double precioMin, double precioMax,
                                                     int pagina, int tamano) {
        return videojuegoRepo.buscarPorGeneroYPrecio(genero, precioMin, precioMax, pagina, tamano);
    }

    public List<Object[]> juegosMejorValorados(double puntuacionMinima) {
        return videojuegoRepo.juegosMejorValorados(puntuacionMinima);
    }

    // ===== EXPORTAR A JSON (Jackson) =====

    public String exportarComoJson(Long id) {
        Optional<Videojuego> v = videojuegoRepo.buscarPorId(id);
        if (v.isEmpty()) throw new IllegalArgumentException("Videojuego no encontrado: " + id);
        VideojuegoDTO dto = new VideojuegoDTO(v.get());
        return JsonUtil.toJson(dto);
    }

    public String exportarTodosComoJson() {
        List<VideojuegoDTO> dtos = videojuegoRepo.listarTodos()
            .stream()
            .map(VideojuegoDTO::new)
            .collect(Collectors.toList());
        return JsonUtil.toJson(dtos);
    }

    public Videojuego importarDesdeJson(String json) {
        VideojuegoDTO dto = JsonUtil.fromJson(json, VideojuegoDTO.class);
        Videojuego v = new Videojuego(
            dto.getTitulo(), dto.getGenero(), dto.getDesarrollador(),
            dto.getFechaLanzamiento(), dto.getPrecio(), dto.getDescripcion()
        );
        return crear(v);
    }

    // ===== VALIDACIONES =====

    private void validar(Videojuego v) {
        if (v.getTitulo() == null || v.getTitulo().isBlank())
            throw new IllegalArgumentException("El título es obligatorio");
        if (v.getGenero() == null || v.getGenero().isBlank())
            throw new IllegalArgumentException("El género es obligatorio");
        if (v.getDesarrollador() == null || v.getDesarrollador().isBlank())
            throw new IllegalArgumentException("El desarrollador es obligatorio");
        if (v.getPrecio() < 0)
            throw new IllegalArgumentException("El precio no puede ser negativo");
    }
}
