package com.videogames.repository;

import com.videogames.domain.Resena;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ResenaRepository {

    public Resena guardar(Resena resena) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (resena.getId() == null) {
                em.persist(resena);
            } else {
                resena = em.merge(resena);
            }
            em.getTransaction().commit();
            return resena;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al guardar reseña: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<Resena> buscarPorId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Resena.class, id));
        } finally {
            em.close();
        }
    }

    public List<Resena> listarPorVideojuego(Long videojuegoId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Resena r JOIN r.videojuego v WHERE v.id = :vid ORDER BY r.fechaResena DESC",
                Resena.class
            )
            .setParameter("vid", videojuegoId)
            .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Resena> listarPorUsuario(Long usuarioId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                "SELECT r FROM Resena r JOIN r.usuario u WHERE u.id = :uid ORDER BY r.fechaResena DESC",
                Resena.class
            )
            .setParameter("uid", usuarioId)
            .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Resena r = em.find(Resena.class, id);
            if (r == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(r);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar reseña: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
