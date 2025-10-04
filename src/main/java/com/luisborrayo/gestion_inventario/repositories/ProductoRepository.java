package com.luisborrayo.gestion_inventario.repositories;

import com.luisborrayo.gestion_inventario.models.Categoria;
import com.luisborrayo.gestion_inventario.models.MovimientoStock;
import com.luisborrayo.gestion_inventario.models.Productos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProductoRepository {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(Productos producto) {
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que 0.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo.");
        }
        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("El producto debe tener categoría.");
        }

        if (producto.getId() == null) {
            em.persist(producto);
        } else {
            em.merge(producto);
        }
    }


    public Productos findById(Long id) {
        return em.find(Productos.class, id);
    }

    @Transactional
    public void delete(Long id) {
        Productos producto = em.find(Productos.class, id);
        if (producto != null) {
            em.remove(producto);
        }
    }

    // 🔹 Total de productos
    public long count() {
        return em.createQuery("SELECT COUNT(p) FROM Productos p", Long.class)
                .getSingleResult();
    }

    // 🔹 Productos con stock bajo
    public long countStockBajo(int umbral) {
        return em.createQuery("SELECT COUNT(p) FROM Productos p WHERE p.stock < :umbral", Long.class)
                .setParameter("umbral", umbral)
                .getSingleResult();
    }

    // 🔹 Productos inactivos
    public long countInactivos() {
        return em.createQuery("SELECT COUNT(p) FROM Productos p WHERE p.estado = :estado", Long.class)
                .setParameter("estado", Productos.Estado.Inactivo)
                .getSingleResult();
    }

    @Transactional
    public void update(Productos producto) {
        if (producto.getId() != null) {
            em.merge(producto);
        }else{
            throw new IllegalArgumentException("No se pudo actualizar el producto");
        }
    }

    public List<Productos> findByStock(Integer stock) {
        return em.createQuery("SELECT p FROM Productos p WHERE p.stock = :stock", Productos.class)
                .setParameter("stock", stock)
                .getResultList();
    }

    public List<Productos> findAll() {
        return em.createQuery("SELECT p FROM Productos p", Productos.class).getResultList();
    }

    public boolean existePorNombreYCategoria(String nombre, Long categoriaId) {
        Long count = em.createQuery(
                        "SELECT COUNT(p) FROM Productos p WHERE p.nombre = :nombre AND p.categoria.id = :categoriaId",
                        Long.class
                )
                .setParameter("nombre", nombre)
                .setParameter("categoriaId", categoriaId)
                .getSingleResult();

        return count > 0;
    }

    public List<Productos> buscarconFiltro(String nombre, Categoria categoria, BigDecimal preciomin, BigDecimal preciomax,
                                           Productos.Estado estado, Integer stockmin, Integer stockmax, int page, int pageSize, String sortBy,
                                           boolean asc) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Productos> cq = cb.createQuery(Productos.class);
        Root<Productos> root = cq.from(Productos.class);
        List<Predicate> predicates = new ArrayList<>();

        if (nombre != null && !nombre.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%"));
        }
        if (categoria != null) {
            predicates.add(cb.equal(root.get("categoria"), categoria));
        }
        if (preciomin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), preciomin));
        }
        if (preciomax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("precio"), preciomax));
        }
        if (estado != null) {
            predicates.add(cb.equal(root.get("estado"), estado));
        }
        if (stockmin != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("stock"), stockmin));
        }
        if (stockmax != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("stock"), stockmax));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        if (sortBy != null && !sortBy.isEmpty()) {
            cq.orderBy(asc ? cb.asc(root.get(sortBy)) : cb.desc(root.get(sortBy)));
        }

        //Paginación y ordenamiento.
        TypedQuery<Productos> query = em.createQuery(cq);
        query.setFirstResult(page * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public Long getTotalProductos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Productos> root = query.from(Productos.class);
        query.select(cb.count(root));
        return em.createQuery(query).getSingleResult();
    }

    public Long getProductosStockBajo(Integer umbral) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Productos> root = query.from(Productos.class);
        query.select(cb.count(root));
        query.where(cb.lessThanOrEqualTo(root.get("stock"), umbral));
        return em.createQuery(query).getSingleResult();
    }

    public Long getProductosInactivos() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Productos> root = query.from(Productos.class);
        query.select(cb.count(root));
        query.where(cb.equal(root.get("estado"), Productos.Estado.Inactivo));
        return em.createQuery(query).getSingleResult();
    }

    public Long getTotalCategorias() {
        String jpql = "SELECT COUNT(DISTINCT p.categoria) FROM Productos p";
        return em.createQuery(jpql, Long.class).getSingleResult();
    }

    public Long getMovimientosEstaSemana() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<MovimientoStock> root = query.from(MovimientoStock.class);

        LocalDate inicioSemana = LocalDate.now().minusDays(7);

        query.select(cb.count(root));
        query.where(cb.greaterThanOrEqualTo(root.get("fecha"), inicioSemana));

        return em.createQuery(query).getSingleResult();
    }

    public LocalDate getFechaUltimaActualizacion() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LocalDate> query = cb.createQuery(LocalDate.class);
        Root<MovimientoStock> root = query.from(MovimientoStock.class);

        query.select(cb.greatest(root.<LocalDate>get("fecha")));

        try {
            return em.createQuery(query).getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
