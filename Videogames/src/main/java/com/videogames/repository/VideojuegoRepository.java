package com.videogames.repository;

import com.videogames.domain.Videojuego;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class VideojuegoRepository {

    // ===== CRUD =====

    public Videojuego guardar(Videojuego videojuego) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (videojuego.getId() == null) {
                em.persist(videojuego);
            } else {
                videojuego = em.merge(videojuego);
            }
            em.getTransaction().commit();
            return videojuego;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al guardar videojuego: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<Videojuego> buscarPorId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Videojuego.class, id));
        } finally {
            em.close();
        }
    }

    public List<Videojuego> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT v FROM Videojuego v ORDER BY v.titulo", Videojuego.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Videojuego v = em.find(Videojuego.class, id);
            if (v == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(v);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar videojuego: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // ===== CONSULTAS AVANZADAS =====

    public List<Videojuego> buscarPorGeneroYPrecio(String genero, double precioMin, double precioMax,
                                                    int pagina, int tamano) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            TypedQuery<Videojuego> query = em.createQuery(
                "SELECT v FROM Videojuego v " +
                "WHERE (:genero IS NULL OR LOWER(v.genero) = LOWER(:genero)) " +
                "AND v.precio BETWEEN :precioMin AND :precioMax " +
                "ORDER BY v.precio ASC",
                Videojuego.class
            );
            query.setParameter("genero", genero);
            query.setParameter("precioMin", precioMin);
            query.setParameter("precioMax", precioMax);
            query.setFirstResult(pagina * tamano);
            query.setMaxResults(tamano);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Object[]> juegosMejorValorados(double puntuacionMinima) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT v.titulo, v.genero, AVG(r.puntuacion), COUNT(r) " +
                "FROM Videojuego v JOIN v.resenas r " +
                "GROUP BY v.id, v.titulo, v.genero " +
                "HAVING AVG(r.puntuacion) >= :puntuacion " +
                "ORDER BY AVG(r.puntuacion) DESC",
                Object[].class
            )
            .setParameter("puntuacion", puntuacionMinima)
            .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Videojuego> buscarPorTitulo(String titulo) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT v FROM Videojuego v WHERE LOWER(v.titulo) LIKE LOWER(:titulo) ORDER BY v.titulo",
                Videojuego.class
            )
            .setParameter("titulo", "%" + titulo + "%")
            .getResultList();
        } finally {
            em.close();
        }
    }
}
