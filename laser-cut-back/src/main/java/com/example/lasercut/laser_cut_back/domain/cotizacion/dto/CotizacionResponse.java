package com.example.lasercut.laser_cut_back.domain.cotizacion.dto;

public class CotizacionResponse {

    private String material;
    private double ancho;
    private double alto;
    private double espesor;
    private double peso;
    private double precioUnitario;
    private int cantidad;
    private String unidad;
    private double precioTotal;

    public CotizacionResponse() {
    }

    public CotizacionResponse(String material, double ancho, double alto, double espesor, double peso, double precioUnitario, int cantidad, String unidad, double precioTotal) {
        this.material = material;
        this.ancho = ancho;
        this.alto = alto;
        this.espesor = espesor;
        this.peso = peso;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.precioTotal = precioTotal;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public double getAncho() {
        return ancho;
    }

    public void setAncho(double ancho) {
        this.ancho = ancho;
    }

    public double getAlto() {
        return alto;
    }

    public void setAlto(double alto) {
        this.alto = alto;
    }

    public double getEspesor() {
        return espesor;
    }

    public void setEspesor(double espesor) {
        this.espesor = espesor;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getPrecioTotal() {
        return precioTotal;
    }

    public void setPrecioTotal(double precioTotal) {
        this.precioTotal = precioTotal;
    }
    
}
