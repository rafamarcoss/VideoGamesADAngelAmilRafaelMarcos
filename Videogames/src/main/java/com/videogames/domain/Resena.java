package com.videogames.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;


@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int puntuacion; // 1 a 10

    @Column(nullable = false, length = 2000)
    private String comentario;

    @Column(name = "fecha_resena", nullable = false)
    private LocalDateTime fechaResena;

    // Relación ManyToOne con Videojuego
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "videojuego_id", nullable = false)
    private Videojuego videojuego;

    // Relación ManyToOne con Usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    public Resena() {}

    public Resena(int puntuacion, String comentario, Videojuego videojuego, Usuario usuario) {
        if (puntuacion < 1 || puntuacion > 10) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 10");
        }
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.videojuego = videojuego;
        this.usuario = usuario;
        this.fechaResena = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) {
        if (puntuacion < 1 || puntuacion > 10)
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 10");
        this.puntuacion = puntuacion;
    }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getFechaResena() { return fechaResena; }
    public void setFechaResena(LocalDateTime fechaResena) { this.fechaResena = fechaResena; }

    public Videojuego getVideojuego() { return videojuego; }
    public void setVideojuego(Videojuego videojuego) { this.videojuego = videojuego; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    @Override
    public String toString() {
        return String.format("[ID:%d] Puntuacion:%d/10 | %s", id, puntuacion, comentario);
    }
}
