package com.luisborrayo.gestion_inventario.beans;

import com.luisborrayo.gestion_inventario.models.Categoria;
import com.luisborrayo.gestion_inventario.services.InventarioService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jdk.jfr.Name;

import java.io.Serializable;
import java.util.List;


@Named("categoriaBean")
@ViewScoped
public class CategoriaBean implements Serializable {
    @Inject
    private InventarioService inventarioService;

    private List<Categoria> lista;
    private Categoria categoriaNueva;
    private Categoria categoriaSeleccionada;

    @PostConstruct
    public void init() {
        lista = inventarioService.listarCategorias();
        categoriaNueva = new Categoria();
    }

    public void guardar() {
        if (categoriaNueva.getId() == null) {
            inventarioService.agregarCategoria(categoriaNueva);
        } else {
            inventarioService.editarCategoria(categoriaNueva);
        }
        lista = inventarioService.listarCategorias();
        categoriaNueva = new Categoria();
    }

    public void eliminar(Long id) {
        inventarioService.eliminarCategoria(id);
        lista = inventarioService.listarCategorias();
    }

    public void prepararEdicion(Categoria c) {
        this.categoriaNueva = c;
    }

    public List<Categoria> getLista() { return lista; }
    public Categoria getCategoriaNueva() { return categoriaNueva; }
}