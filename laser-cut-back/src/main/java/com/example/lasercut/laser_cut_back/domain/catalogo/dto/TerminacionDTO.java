package com.example.lasercut.laser_cut_back.domain.catalogo.dto;

public class TerminacionDTO {
    
    private Long id;
    private String nombre;
    private Double precioExtraPorM2;

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

    public Double getPrecioExtraPorM2() {
        return precioExtraPorM2;
    }

    public void setPrecioExtraPorM2(Double precioExtraPorM2) {
        this.precioExtraPorM2 = precioExtraPorM2;
    }

}
