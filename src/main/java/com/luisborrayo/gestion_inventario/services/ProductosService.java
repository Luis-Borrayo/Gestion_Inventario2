package com.luisborrayo.gestion_inventario.services;

import com.luisborrayo.gestion_inventario.models.Productos;
import com.luisborrayo.gestion_inventario.repositories.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Named
@ApplicationScoped
public class ProductosService {
    @Inject
    private ProductoRepository productoRepository;

    @Transactional
    public void save(Productos producto) {
        validarProducto(producto);
        productoRepository.save(producto);
    }

    @Transactional
    public void update(Productos producto) {
        validarProducto(producto);
        if (producto.getId() == null) {
            throw new IllegalArgumentException("Id no puede ser vacio");
        }
        productoRepository.save(producto);
    }

    public Productos findById(Long id) {
        return productoRepository.findById(id);
    }
    public List<Productos> findAll() {
        return productoRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        productoRepository.delete(id);
    }

    private void validarProducto(Productos producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre no puede ser vacio");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Precio no puede ser vacio");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("Stock no puede ser vacio");
        }
        if (producto.getCategoria() == null) {
            throw new IllegalArgumentException("Categoria no puede ser vacio");
        }

        List<Productos> existe = productoRepository.buscarconFiltro(
                producto.getNombre(), producto.getCategoria(), null, null,
                null, null, null,
                0, 1,
                null, true
        );
        if(!existe.isEmpty() && (producto.getId() == null || !existe.get(0).getId().equals(producto.getId()))) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre en la categoría seleccionada.");
        }
    }
}
