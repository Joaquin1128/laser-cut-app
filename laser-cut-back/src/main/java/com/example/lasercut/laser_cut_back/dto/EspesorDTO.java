package com.example.lasercut.laser_cut_back.dto;

import java.util.List;

public class EspesorDTO {
    
    private Long id;
    private Double espesorMm;
    private Double espesorInch;
    private Double precioExtraPorM2;
    private List<TerminacionDTO> terminaciones;

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

    public Double getEspesorInch() {
        return espesorInch;
    }

    public void setEspesorInch(Double espesorInch) {
        this.espesorInch = espesorInch;
    }

    public Double getPrecioExtraPorM2() {
        return precioExtraPorM2;
    }

    public void setPrecioExtraPorM2(Double precioExtraPorM2) {
        this.precioExtraPorM2 = precioExtraPorM2;
    }

    public List<TerminacionDTO> getTerminaciones() {
        return terminaciones;
    }

    public void setTerminaciones(List<TerminacionDTO> terminaciones) {
        this.terminaciones = terminaciones;
    }

}
