package com.videogames.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class AuditLogRepository {

    private static final String COLLECTION = "audit_logs";
    private final MongoCollection<Document> collection;

    public AuditLogRepository() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.collection = db.getCollection(COLLECTION);
        // Crear índices para mejorar rendimiento de consultas
        collection.createIndex(Indexes.ascending("entityId", "entityType"));
        collection.createIndex(Indexes.descending("timestamp"));
        collection.createIndex(Indexes.ascending("type"));
    }

    public void registrar(String tipo, String usuario, String entityType, Long entityId, Document payload) {
        Document log = new Document()
            .append("timestamp", new Date())
            .append("type", tipo)
            .append("user", usuario)
            .append("entityType", entityType)
            .append("entityId", entityId)
            .append("payload", payload);

        collection.insertOne(log);
    }

    public List<Document> buscarPorTipoYEntidad(String tipo, String entityType) {
        List<Document> resultados = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(
            Filters.and(
                Filters.eq("type", tipo),
                Filters.eq("entityType", entityType)
            )
        ).sort(Sorts.descending("timestamp")).limit(50).iterator()) {
            while (cursor.hasNext()) {
                resultados.add(cursor.next());
            }
        }
        return resultados;
    }

    public List<Document> buscarPorUsuarioYFecha(String usuario, LocalDateTime desde, LocalDateTime hasta) {
        List<Document> resultados = new ArrayList<>();
        Date desdeDate = Date.from(desde.atZone(ZoneId.systemDefault()).toInstant());
        Date hastaDate = Date.from(hasta.atZone(ZoneId.systemDefault()).toInstant());

        try (MongoCursor<Document> cursor = collection.find(
            Filters.and(
                Filters.eq("user", usuario),
                Filters.gte("timestamp", desdeDate),
                Filters.lte("timestamp", hastaDate)
            )
        ).sort(Sorts.descending("timestamp")).iterator()) {
            while (cursor.hasNext()) {
                resultados.add(cursor.next());
            }
        }
        return resultados;
    }

    public List<Document> contarAccionesPorTipo() {
        List<Document> resultados = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.aggregate(
            Arrays.asList(
                Aggregates.group("$type", Accumulators.sum("total", 1)),
                Aggregates.sort(Sorts.descending("total"))
            )
        ).iterator()) {
            while (cursor.hasNext()) {
                resultados.add(cursor.next());
            }
        }
        return resultados;
    }

    public List<Document> listarRecientes(int limite) {
        List<Document> resultados = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find()
             .sort(Sorts.descending("timestamp"))
             .limit(limite)
             .iterator()) {
            while (cursor.hasNext()) {
                resultados.add(cursor.next());
            }
        }
        return resultados;
    }

    public long contarTotal() {
        return collection.countDocuments();
    }
}
