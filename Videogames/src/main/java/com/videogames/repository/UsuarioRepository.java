package com.videogames.repository;

import com.videogames.domain.Usuario;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class UsuarioRepository {

    public Usuario guardar(Usuario usuario) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            if (usuario.getId() == null) {
                em.persist(usuario);
            } else {
                usuario = em.merge(usuario);
            }
            em.getTransaction().commit();
            return usuario;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al guardar usuario: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    public Optional<Usuario> buscarPorId(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return Optional.ofNullable(em.find(Usuario.class, id));
        } finally {
            em.close();
        }
    }

    public Optional<Usuario> buscarPorUsername(String username) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            List<Usuario> results = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class
            )
            .setParameter("username", username)
            .getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM Usuario u ORDER BY u.username", Usuario.class)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public boolean eliminar(Long id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario u = em.find(Usuario.class, id);
            if (u == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(u);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw new RuntimeException("Error al eliminar usuario: " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
}
