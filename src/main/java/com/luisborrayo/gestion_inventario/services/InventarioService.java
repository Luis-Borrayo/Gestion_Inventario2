package com.luisborrayo.gestion_inventario.services;

import com.luisborrayo.gestion_inventario.Dashboard.DashboardService;
import com.luisborrayo.gestion_inventario.models.Categoria;
import com.luisborrayo.gestion_inventario.models.MovimientoStock;
import com.luisborrayo.gestion_inventario.models.Productos;
import com.luisborrayo.gestion_inventario.repositories.CategoriaRepository;
import com.luisborrayo.gestion_inventario.repositories.MovimientoStockRepository;
import com.luisborrayo.gestion_inventario.repositories.ProductoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Named
@ApplicationScoped
public class InventarioService {

    @Inject
    private ProductosService productosService;

    @Inject
    private CategoriaRepository categoriaRepository;

    @Inject
    private ProductoRepository productoRepository;

    @Inject
    private MovimientoStockRepository movimientoStockRepository;

    @Inject
    private DashboardService dashboardService;

    @Transactional
    public void agregarProducto(Productos producto) {
        productosService.save(producto);
    }

    @Transactional
    public void editarProducto(Productos producto) {
        productosService.update(producto);
    }

    public Productos obtenerProductoPorId(Long id) {
        return productosService.findById(id);
    }

    public List<Productos> listarProductos() {
        return productosService.findAll();
    }

    @Transactional
    public void eliminarProducto(Long id) {
        productosService.delete(id);
    }

    public List<Productos> buscarProductos(String nombre, Categoria categoria,
                                           BigDecimal precioMin, BigDecimal precioMax,
                                           Productos.Estado estado,
                                           Integer stockMin, Integer stockMax,
                                           int pagina, int pageSize,
                                           String ordenarPor, boolean ascendente) {
        return productoRepository.buscarconFiltro(
                nombre, categoria, precioMin, precioMax, estado, stockMin, stockMax,
                pagina, pageSize, ordenarPor, ascendente
        );
    }

    @Transactional
    public void agregarCategoria(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

    @Transactional
    public void editarCategoria(Categoria categoria) {
        categoriaRepository.save(categoria);
    }

    public void eliminarCategoria(Long id) {
        categoriaRepository.delete(id);
    }

    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    @Transactional
    public MovimientoStock registrarEntrada(Long productoId, Integer cantidad, String motivo) {
        return movimientoStockRepository.registrarEntrada(productoId, cantidad, motivo);
    }

    @Transactional
    public MovimientoStock registrarSalida(Long productoId, Integer cantidad, String motivo) {
        return movimientoStockRepository.registrarSalida(productoId, cantidad, motivo);
    }

    public List<MovimientoStock> listarMovimientos(Long productoId, String tipo,
                                                   LocalDate fechaDesde, LocalDate fechaHasta) {
        return movimientoStockRepository.listarConFiltros(productoId, tipo, fechaDesde, fechaHasta);
    }

    public long totalProductos() {
        return dashboardService.getTotalProductos();
    }

    public long productosStockBajo(int umbral) {
        return dashboardService.getProductosStockBajo(umbral);
    }

    public long productosInactivos() {
        return dashboardService.getProductosInactivos();
    }

    public long totalCategorias() {
        return dashboardService.getTotalCategorias();
    }

    public long movimientosEstaSemana() {
        return dashboardService.getMovimientosSemana();
    }
}
