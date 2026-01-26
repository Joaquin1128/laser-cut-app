package com.example.lasercut.laser_cut_back.domain.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "terminacion")
public class Terminacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "precio_extra_por_m2", nullable = false)
    private Double precioExtraPorM2 = 0.0;

    @ManyToOne
    @JoinColumn(name = "espesor_id", nullable = false)
    private Espesor espesor;

    public Terminacion() {
    }

    public Terminacion(Long id, String nombre, Double precioExtraPorM2, Espesor espesor) {
        this.id = id;
        this.nombre = nombre;
        this.precioExtraPorM2 = precioExtraPorM2;
        this.espesor = espesor;
    }

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

    public Espesor getEspesor() {
        return espesor;
    }

    public void setEspesor(Espesor espesor) {
        this.espesor = espesor;
    }

}
