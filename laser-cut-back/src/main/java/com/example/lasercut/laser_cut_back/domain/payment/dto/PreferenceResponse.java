package com.example.lasercut.laser_cut_back.domain.payment.dto;

/**
 * DTO para respuesta de preferencia de Mercado Pago
 */
public class PreferenceResponse {
    
    private String id;
    private String initPoint;
    private String sandboxInitPoint;
    private String clientId;

    public PreferenceResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInitPoint() {
        return initPoint;
    }

    public void setInitPoint(String initPoint) {
        this.initPoint = initPoint;
    }

    public String getSandboxInitPoint() {
        return sandboxInitPoint;
    }

    public void setSandboxInitPoint(String sandboxInitPoint) {
        this.sandboxInitPoint = sandboxInitPoint;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

}
