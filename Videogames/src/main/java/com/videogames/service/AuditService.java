package com.videogames.service;

import com.videogames.mongo.AuditLogRepository;
import org.bson.Document;

import java.time.LocalDateTime;
import java.util.List;

public class AuditService {

    private final AuditLogRepository auditRepo;

    public AuditService() {
        this.auditRepo = new AuditLogRepository();
    }

    public List<Document> listarRecientes(int limite) {
        return auditRepo.listarRecientes(limite);
    }

    public List<Document> buscarPorTipoYEntidad(String tipo, String entidad) {
        return auditRepo.buscarPorTipoYEntidad(tipo, entidad);
    }

    public List<Document> buscarPorUsuarioYFecha(String usuario, LocalDateTime desde, LocalDateTime hasta) {
        return auditRepo.buscarPorUsuarioYFecha(usuario, desde, hasta);
    }

    public List<Document> estadisticasPorTipo() {
        return auditRepo.contarAccionesPorTipo();
    }

    public long totalEventos() {
        return auditRepo.contarTotal();
    }
}
