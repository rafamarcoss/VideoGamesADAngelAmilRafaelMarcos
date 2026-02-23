package com.videogames.domain;

import java.time.LocalDate;


public class VideojuegoDTO {

    private Long id;
    private String titulo;
    private String genero;
    private String desarrollador;
    private LocalDate fechaLanzamiento;
    private double precio;
    private String descripcion;
    private int totalResenas;
    private double puntuacionMedia;

    public VideojuegoDTO() {}

    public VideojuegoDTO(Videojuego v) {
        this.id = v.getId();
        this.titulo = v.getTitulo();
        this.genero = v.getGenero();
        this.desarrollador = v.getDesarrollador();
        this.fechaLanzamiento = v.getFechaLanzamiento();
        this.precio = v.getPrecio();
        this.descripcion = v.getDescripcion();
        this.totalResenas = v.getResenas().size();
        this.puntuacionMedia = v.getResenas().stream()
                .mapToInt(Resena::getPuntuacion)
                .average()
                .orElse(0.0);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    public String getDesarrollador() { return desarrollador; }
    public void setDesarrollador(String desarrollador) { this.desarrollador = desarrollador; }

    public LocalDate getFechaLanzamiento() { return fechaLanzamiento; }
    public void setFechaLanzamiento(LocalDate fechaLanzamiento) { this.fechaLanzamiento = fechaLanzamiento; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int getTotalResenas() { return totalResenas; }
    public void setTotalResenas(int totalResenas) { this.totalResenas = totalResenas; }

    public double getPuntuacionMedia() { return puntuacionMedia; }
    public void setPuntuacionMedia(double puntuacionMedia) { this.puntuacionMedia = puntuacionMedia; }

    @Override
    public String toString() {
        return String.format("%s | %s | %.2f€ | Media: %.1f/10 (%d reseñas)",
                titulo, genero, precio, puntuacionMedia, totalResenas);
    }
}
