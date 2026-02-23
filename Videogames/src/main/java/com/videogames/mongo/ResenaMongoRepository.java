package com.videogames.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Accumulators;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ResenaMongoRepository {

    private static final String COLLECTION = "resenas_historial";
    private final MongoCollection<Document> collection;

    public ResenaMongoRepository() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.collection = db.getCollection(COLLECTION);
        collection.createIndex(Indexes.ascending("videojuegoId"));
        collection.createIndex(Indexes.ascending("usuarioId"));
        collection.createIndex(Indexes.descending("fechaCreacion"));
    }

    public void guardarSnapshot(Long resenaId, Long videojuegoId, String tituloJuego,
                                 Long usuarioId, String usernameUsuario,
                                 int puntuacion, String comentario) {
        Document doc = new Document()
            .append("resenaId", resenaId)
            .append("videojuegoId", videojuegoId)
            .append("tituloJuego", tituloJuego)
            .append("usuarioId", usuarioId)
            .append("username", usernameUsuario)
            .append("puntuacion", puntuacion)
            .append("comentario", comentario)
            .append("fechaCreacion", new java.util.Date());

        collection.insertOne(doc);
    }

    public List<Document> buscarPorVideojuego(Long videojuegoId) {
        List<Document> resultado = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(
            Filters.eq("videojuegoId", videojuegoId)
        ).sort(Sorts.descending("fechaCreacion")).iterator()) {
            while (cursor.hasNext()) resultado.add(cursor.next());
        }
        return resultado;
    }

    public List<Document> buscarPorUsuario(Long usuarioId) {
        List<Document> resultado = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(
            Filters.eq("usuarioId", usuarioId)
        ).sort(Sorts.descending("fechaCreacion")).iterator()) {
            while (cursor.hasNext()) resultado.add(cursor.next());
        }
        return resultado;
    }

    public List<Document> estadisticasPorJuego() {
        List<Document> resultado = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.aggregate(
            Arrays.asList(
                Aggregates.group("$tituloJuego",
                    Accumulators.avg("media", "$puntuacion"),
                    Accumulators.sum("total", 1)
                ),
                Aggregates.sort(Sorts.descending("media"))
            )
        ).iterator()) {
            while (cursor.hasNext()) resultado.add(cursor.next());
        }
        return resultado;
    }
}
