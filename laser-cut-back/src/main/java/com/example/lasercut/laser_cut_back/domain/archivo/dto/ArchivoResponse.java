package com.example.lasercut.laser_cut_back.domain.archivo.dto;

public class ArchivoResponse {

    private String nombre;
    private double ancho;
    private double alto;
    private String vistaPreviaBase64;

    public ArchivoResponse() {
    }

    public ArchivoResponse(String nombre, double ancho, double alto, String vistaPreviaBase64) {
        this.nombre = nombre;
        this.ancho = ancho;
        this.alto = alto;
        this.vistaPreviaBase64 = vistaPreviaBase64;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public String getVistaPreviaBase64() {
        return vistaPreviaBase64;
    }

    public void setVistaPreviaBase64(String vistaPreviaBase64) {
        this.vistaPreviaBase64 = vistaPreviaBase64;
    }
    
}
