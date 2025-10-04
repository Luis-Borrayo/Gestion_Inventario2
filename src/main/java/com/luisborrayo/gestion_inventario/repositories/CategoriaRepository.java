package com.luisborrayo.gestion_inventario.repositories;

import com.luisborrayo.gestion_inventario.models.Categoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class CategoriaRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Categoria categoria) {
        if (categoria.getId() == null) {
            em.persist(categoria);
        }else {
            em.merge(categoria);
        }
    }

    public Categoria findById(Long id) {
        return em.find(Categoria.class, id);
    }

    @Transactional
    public void delete(Long id) {
        Categoria categoria = findById(id);
        if  (categoria != null) {
            em.remove(categoria);
        }
    }

    public long count() {
        return em.createQuery("SELECT COUNT(c) FROM Categoria c", Long.class)
                .getSingleResult();
    }

    public List<Categoria> findAll() {
        return em.createQuery("SELECT c FROM  Categoria c", Categoria.class).getResultList();
    }

    public Long getTotalCategorías() {
        return em.createQuery("SELECT COUNT(c) FROM  Categoria c", Long.class).getSingleResult();
    }

    public Long getTotalCategorias() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Categoria> root = query.from(Categoria.class);
        query.select(cb.count(root));
        return em.createQuery(query).getSingleResult();
    }
}