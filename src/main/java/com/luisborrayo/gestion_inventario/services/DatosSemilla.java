package com.luisborrayo.gestion_inventario.services;

import com.luisborrayo.gestion_inventario.models.Categoria;
import com.luisborrayo.gestion_inventario.models.Productos;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DatosSemilla {

    @Inject
    private InventarioService inventarioService;

    @PostConstruct
    @Transactional
    public void init() {
        Categoria catElectro = new Categoria("Electrónica");
        Categoria catHogar = new Categoria("Hogar");
        Categoria catOficina = new Categoria("Oficina");

        inventarioService.agregarCategoria(catElectro);
        inventarioService.agregarCategoria(catHogar);
        inventarioService.agregarCategoria(catOficina);

        Productos p1 = new Productos(
                "Televisor 42''",
                catElectro,
                new BigDecimal("450.00"),
                LocalDate.now().atStartOfDay(),
                10,
                Productos.Estado.Activo
        );

        Productos p2 = new Productos(
                "Laptop Gamer",
                catElectro,
                new BigDecimal("1200.00"),
                LocalDate.now().atStartOfDay(),
                5,
                Productos.Estado.Activo
        );

        Productos p3 = new Productos(
                "Silla Oficina",
                catOficina,
                new BigDecimal("150.00"),
                LocalDate.now().atStartOfDay(),
                20,
                Productos.Estado.Activo
        );

        Productos p4 = new Productos(
                "Lámpara de Mesa",
                catHogar,
                new BigDecimal("30.00"),
                LocalDate.now().atStartOfDay(),
                15,
                Productos.Estado.Activo
        );

        inventarioService.agregarProducto(p1);
        inventarioService.agregarProducto(p2);
        inventarioService.agregarProducto(p3);
        inventarioService.agregarProducto(p4);

        inventarioService.registrarEntrada(p1.getId(), 5, "Ingreso inicial");
        inventarioService.registrarEntrada(p2.getId(), 2, "Ingreso inicial");
        inventarioService.registrarEntrada(p3.getId(), 10, "Ingreso inicial");
        inventarioService.registrarEntrada(p4.getId(), 5, "Ingreso inicial");

        inventarioService.registrarSalida(p1.getId(), 2, "Venta de producto");
        inventarioService.registrarSalida(p3.getId(), 5, "Entrega a cliente");
    }
}
