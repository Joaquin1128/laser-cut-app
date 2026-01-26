package com.example.lasercut.laser_cut_back.domain.catalogo.dto;

import java.util.List;

public class MaterialDTO {
    
    private Long id;
    private String nombre;
    private String tipo;
    private Double densidad;
    private Double precioPorKg;
    private List<EspesorDTO> espesores;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Double getDensidad() {
        return densidad;
    }

    public void setDensidad(Double densidad) {
        this.densidad = densidad;
    }

    public Double getPrecioPorKg() {
        return precioPorKg;
    }

    public void setPrecioPorKg(Double precioPorKg) {
        this.precioPorKg = precioPorKg;
    }

    public List<EspesorDTO> getEspesores() {
        return espesores;
    }

    public void setEspesores(List<EspesorDTO> espesores) {
        this.espesores = espesores;
    }

}
