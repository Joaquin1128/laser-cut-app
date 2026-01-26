package com.example.lasercut.laser_cut_back.domain.catalogo.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "espesor")
public class Espesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "espesor_mm", nullable = false)
    private Double espesorMm;

    @Column(name = "precio_extra_por_m2", nullable = false)
    private Double precioExtraPorM2 = 0.0;

    @ManyToOne
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @OneToMany(mappedBy = "espesor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Terminacion> terminaciones = new HashSet<>();

    public Espesor() {
    }

    public Espesor(Long id, Double espesorMm, Double precioExtraPorM2, Material material, Set<Terminacion> terminaciones) {
        this.id = id;
        this.espesorMm = espesorMm;
        this.precioExtraPorM2 = precioExtraPorM2;
        this.material = material;
        this.terminaciones = terminaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getEspesorMm() {
        return espesorMm;
    }

    public void setEspesorMm(Double espesorMm) {
        this.espesorMm = espesorMm;
    }

    public Double getPrecioExtraPorM2() {
        return precioExtraPorM2;
    }

    public void setPrecioExtraPorM2(Double precioExtraPorM2) {
        this.precioExtraPorM2 = precioExtraPorM2;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public Set<Terminacion> getTerminaciones() {
        return terminaciones;
    }

    public void setTerminaciones(Set<Terminacion> terminaciones) {
        this.terminaciones = terminaciones;
    }

}
