package com.example.lasercut.laser_cut_back.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Tramos de margen de ganancia de la plataforma sobre el precio de fábrica.
 * Se evalúan en orden: se aplica el primer tramo cuyo precioHasta >= precioFabrica.
 * Si el precio supera todos los tramos, se usa el último.
 *
 * margen=1.0  → el cliente paga el doble del precio fábrica  (100% de ganancia)
 * margen=1.5  → el cliente paga 2.5× el precio fábrica       (150% de ganancia)
 */
@Component
@ConfigurationProperties(prefix = "pricing.margen")
public class MargenConfig {

    private List<Tramo> tramos = new ArrayList<>();

    public double resolverMargen(double precioFabrica) {
        for (Tramo t : tramos) {
            if (precioFabrica <= t.getPrecioHasta()) {
                return t.getMargen();
            }
        }
        if (!tramos.isEmpty()) {
            return tramos.get(tramos.size() - 1).getMargen();
        }
        return 0.0;
    }

    public List<Tramo> getTramos() {
        return tramos;
    }

    public void setTramos(List<Tramo> tramos) {
        this.tramos = tramos;
    }

    public static class Tramo {

        private double precioHasta;
        private double margen;

        public double getPrecioHasta() {
            return precioHasta;
        }

        public void setPrecioHasta(double precioHasta) {
            this.precioHasta = precioHasta;
        }

        public double getMargen() {
            return margen;
        }

        public void setMargen(double margen) {
            this.margen = margen;
        }
        
    }

}
