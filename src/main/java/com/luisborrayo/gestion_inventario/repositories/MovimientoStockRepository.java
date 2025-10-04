package com.luisborrayo.gestion_inventario.repositories;

import com.luisborrayo.gestion_inventario.models.MovimientoStock;
import com.luisborrayo.gestion_inventario.models.Productos;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository para Movimientos de Stock
 */

@ApplicationScoped
public class MovimientoStockRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(MovimientoStock movimiento) {
        em.persist(movimiento);
    }

    public long countByFechaDesde(LocalDateTime fecha) {
        return em.createQuery(
                        "SELECT COUNT(m) FROM MovimientoStock m WHERE m.fecha >= :fecha", Long.class)
                .setParameter("fecha", fecha)
                .getSingleResult();
    }

    public LocalDateTime findMaxFecha() {
        return em.createQuery(
                        "SELECT MAX(m.fecha) FROM MovimientoStock m", LocalDateTime.class)
                .getSingleResult();
    }

    @Transactional
    public MovimientoStock registrarEntrada(Long productoId, Integer cantidad, String motivo) {
        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(productoId);
        movimiento.setTipo("ENTRADA");
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFecha(LocalDate.now());
        em.persist(movimiento);

        // LÍNEA VERDE: Actualizar stockActual del producto
        Productos producto = em.find(Productos.class, productoId);
        if (producto != null) {
            producto.setStock(producto.getStock() + cantidad);
            em.merge(producto);
        }

        return movimiento;
    }


    @Transactional
    public MovimientoStock registrarSalida(Long productoId, Integer cantidad, String motivo) {
        Productos producto = em.find(Productos.class, productoId);
        if (producto == null) {
            throw new IllegalArgumentException("Producto no encontrado");
        }

        if (producto.getEstado() == Productos.Estado.Inactivo) {
            throw new IllegalStateException("El producto está inactivo y no admite salidas.");
        }

        if (producto.getStock() < cantidad) {
            throw new IllegalArgumentException("Stock insuficiente. Stock actual: " +
                    producto.getStock() + ", cantidad solicitada: " + cantidad);
        }

        MovimientoStock movimiento = new MovimientoStock();
        movimiento.setProductoId(productoId);
        movimiento.setTipo("SALIDA");
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivo);
        movimiento.setFecha(LocalDate.now());
        em.persist(movimiento);

        producto.setStock(producto.getStock() - cantidad);
        em.merge(producto);

        return movimiento;
    }

    public List<MovimientoStock> listarConFiltros(
            Long productoId,
            String tipo,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MovimientoStock> query = cb.createQuery(MovimientoStock.class);
        Root<MovimientoStock> root = query.from(MovimientoStock.class);

        List<Predicate> predicates = new ArrayList<>();

        if (productoId != null) {
            predicates.add(cb.equal(root.get("productoId"), productoId));
        }

        if (tipo != null && !tipo.isEmpty()) {
            predicates.add(cb.equal(root.get("tipo"), tipo));
        }

        if (fechaDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fecha"), fechaDesde));
        }
        if (fechaHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fecha"), fechaHasta));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get("fecha")));

        return em.createQuery(query).getResultList();
    }

    public List<MovimientoStock> findAll() {
        return em.createQuery("SELECT m FROM MovimientoStock m ORDER BY m.fecha DESC",
                MovimientoStock.class).getResultList();
    }

    public MovimientoStock findById(Long id) {
        return em.find(MovimientoStock.class, id);
    }


    public List<MovimientoStock> getUltimosMovimientos(int cantidad) {
        return em.createQuery(
                        "SELECT m FROM MovimientoStock m ORDER BY m.fecha DESC",
                        MovimientoStock.class)
                .setMaxResults(cantidad)
                .getResultList();
    }

    public Long contarMovimientosPorProducto(Long productoId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<MovimientoStock> root = query.from(MovimientoStock.class);

        query.select(cb.count(root));
        query.where(cb.equal(root.get("productoId"), productoId));

        return em.createQuery(query).getSingleResult();
    }
}