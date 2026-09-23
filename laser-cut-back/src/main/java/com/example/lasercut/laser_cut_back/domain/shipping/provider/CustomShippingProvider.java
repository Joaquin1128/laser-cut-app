package com.example.lasercut.laser_cut_back.domain.shipping.provider;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteResponse;

/**
 * Cotizador de envíos propio basado en zonificación inteligente por Código Postal y Provincia.
 * 
 * Implementa una estructura tarifaria por escalones con tarifas aplanadas para larga distancia,
 * similar al modelo logístico de paquetería nacional en Argentina (Andreani / Correo Argentino):
 * 
 * - Zona 1 (Local / AMBA): CABA y cordón GBA cercano.
 * - Zona 2 (Regional): Resto de Prov. Buenos Aires, Santa Fe, Córdoba, Entre Ríos.
 * - Zona 3 (Nacional / Aplanada): Cuyo, Litoral, NOA, NEA y resto del país.
 * - Zona 4 (Patagonia y Extremo Sur): Neuquén, Río Negro, Chubut, Santa Cruz, Tierra del Fuego.
 * 
 * Los valores base, costos por kg extra y tiempos de entrega son 100% configurables desde application.properties.
 */
@Component("customShippingProvider")
public class CustomShippingProvider implements ShippingProvider {

    public enum ZonaEnvio {
        LOCAL,
        REGIONAL,
        NACIONAL,
        PATAGONIA
    }

    // Configuración Zona Local (AMBA)
    @Value("${shipping.local.costo-base:6000.0}")
    private BigDecimal localCostoBase;

    @Value("${shipping.local.costo-por-kg:500.0}")
    private BigDecimal localCostoPorKg;

    @Value("${shipping.local.dias-estimados:2 a 4 días hábiles}")
    private String localDiasEstimados;

    // Configuración Zona Regional (Centro)
    @Value("${shipping.regional.costo-base:9000.0}")
    private BigDecimal regionalCostoBase;

    @Value("${shipping.regional.costo-por-kg:800.0}")
    private BigDecimal regionalCostoPorKg;

    @Value("${shipping.regional.dias-estimados:3 a 5 días hábiles}")
    private String regionalDiasEstimados;

    // Configuración Zona Nacional (Aplanada)
    @Value("${shipping.nacional.costo-base:12500.0}")
    private BigDecimal nacionalCostoBase;

    @Value("${shipping.nacional.costo-por-kg:1000.0}")
    private BigDecimal nacionalCostoPorKg;

    @Value("${shipping.nacional.dias-estimados:4 a 7 días hábiles}")
    private String nacionalDiasEstimados;

    // Configuración Zona Patagonia Sur
    @Value("${shipping.patagonia.costo-base:16000.0}")
    private BigDecimal patagoniaCostoBase;

    @Value("${shipping.patagonia.costo-por-kg:1400.0}")
    private BigDecimal patagoniaCostoPorKg;

    @Value("${shipping.patagonia.dias-estimados:6 a 10 días hábiles}")
    private String patagoniaDiasEstimados;

    // Peso base incluido sin recargo (en kg)
    @Value("${shipping.peso-base-incluido:2.0}")
    private double pesoBaseIncluido;

    private static final Pattern CP_NUM_PATTERN = Pattern.compile("(\\d{4})");

    @Override
    public ShippingQuoteResponse calculateShipping(ShippingQuoteRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Shipping quote request is required");
        }

        double totalWeight = (request.getTotalWeight() != null && request.getTotalWeight() > 0)
                ? request.getTotalWeight()
                : 1.0;

        ZonaEnvio zona = determinarZona(request.getPostalCode(), request.getProvince());

        BigDecimal costoBase;
        BigDecimal costoPorKg;
        String diasEstimados;

        switch (zona) {
            case LOCAL:
                costoBase = localCostoBase;
                costoPorKg = localCostoPorKg;
                diasEstimados = localDiasEstimados;
                break;
            case REGIONAL:
                costoBase = regionalCostoBase;
                costoPorKg = regionalCostoPorKg;
                diasEstimados = regionalDiasEstimados;
                break;
            case PATAGONIA:
                costoBase = patagoniaCostoBase;
                costoPorKg = patagoniaCostoPorKg;
                diasEstimados = patagoniaDiasEstimados;
                break;
            case NACIONAL:
            default:
                costoBase = nacionalCostoBase;
                costoPorKg = nacionalCostoPorKg;
                diasEstimados = nacionalDiasEstimados;
                break;
        }

        double pesoExcedente = Math.max(0.0, totalWeight - pesoBaseIncluido);
        BigDecimal costoExcedente = costoPorKg.multiply(BigDecimal.valueOf(pesoExcedente));
        BigDecimal costoFinal = costoBase.add(costoExcedente).setScale(2, RoundingMode.HALF_UP);

        ShippingQuoteResponse response = new ShippingQuoteResponse(
                costoFinal,
                getProviderName(),
                diasEstimados
        );
        response.setMessage(generarMensajeZona(zona, totalWeight));

        return response;
    }

    /**
     * Determina la zona geográfica a partir del Código Postal y la Provincia ingresados.
     */
    public ZonaEnvio determinarZona(String postalCode, String province) {
        String cpNormalizado = postalCode != null ? postalCode.trim().toUpperCase() : "";
        String provNormalizada = normalizarTexto(province);

        // 1. Detectar por provincia explícita si coincide con Patagonia o Cuyo/NOA/NEA/La Pampa
        if (esProvinciaPatagonia(provNormalizada)) {
            return ZonaEnvio.PATAGONIA;
        }
        if (esProvinciaNacional(provNormalizada)) {
            return ZonaEnvio.NACIONAL;
        }

        // 2. Extraer prefijo de letra (CPA) y los 4 dígitos numéricos si existen
        Character prefijoLetra = extraerPrefijoLetra(cpNormalizado);
        Integer numeroCp = extraerNumeroCp(cpNormalizado);

        // 3. Evaluar por Código Postal numérico
        if (numeroCp != null) {
            // Patagonia: 8300 a 9499 (Neuquén, Río Negro, Chubut, Santa Cruz, Tierra del Fuego)
            if (numeroCp >= 8300 && numeroCp <= 9499) {
                return ZonaEnvio.PATAGONIA;
            }

            // CABA: 1000 a 1499
            if (numeroCp >= 1000 && numeroCp <= 1499) {
                return ZonaEnvio.LOCAL;
            }

            // GBA primer y segundo cordón: 1600 a 1899
            if (numeroCp >= 1600 && numeroCp <= 1899) {
                return ZonaEnvio.LOCAL;
            }

            // Cuyo: San Juan (5400), Mendoza (5500-5699), San Luis (5700-5799)
            if (numeroCp >= 5400 && numeroCp <= 5799) {
                return ZonaEnvio.NACIONAL;
            }

            // La Pampa: 6200 a 6399
            if (numeroCp >= 6200 && numeroCp <= 6399) {
                return ZonaEnvio.NACIONAL;
            }

            // NOA: Tucumán (4000-4199), Santiago del Estero (4200-4399), Salta (4400-4599),
            // Jujuy (4600-4699), Catamarca (4700-4799), La Rioja (5300-5399)
            if (numeroCp >= 4000 && numeroCp <= 4799) {
                return ZonaEnvio.NACIONAL;
            }
            if (numeroCp >= 5300 && numeroCp <= 5399) {
                return ZonaEnvio.NACIONAL;
            }

            // NEA: Misiones (3300-3399), Corrientes (3400-3499), Chaco (3500-3599), Formosa (3600-3699)
            if (numeroCp >= 3300 && numeroCp <= 3699) {
                return ZonaEnvio.NACIONAL;
            }

            // Región Centro / Buenos Aires Interior:
            // Resto Bs As: 1900-2999 y 6000-8299 (excepto La Pampa 6200-6399)
            // Santa Fe: 2000-2499, 3000-3099
            // Córdoba: 5000-5299, 5800-5999
            // Entre Ríos: 3100-3299
            if ((numeroCp >= 1900 && numeroCp <= 3299)
                    || (numeroCp >= 5000 && numeroCp <= 5299)
                    || (numeroCp >= 5800 && numeroCp <= 5999)
                    || (numeroCp >= 6000 && numeroCp <= 8299)) {
                return ZonaEnvio.REGIONAL;
            }
        }

        // Si tiene letra CPA de provincia
        if (prefijoLetra != null) {
            if (prefijoLetra == 'C') {
                return ZonaEnvio.LOCAL;
            }
            if (prefijoLetra == 'Q' || prefijoLetra == 'R' || prefijoLetra == 'U' || prefijoLetra == 'Z' || prefijoLetra == 'V') {
                return ZonaEnvio.PATAGONIA;
            }
            if (prefijoLetra == 'B' || prefijoLetra == 'X' || prefijoLetra == 'S' || prefijoLetra == 'E') {
                return ZonaEnvio.REGIONAL;
            }
            // Letras de Cuyo, NOA, NEA, La Pampa
            return ZonaEnvio.NACIONAL;
        }

        // 4. Fallback por nombre de provincia si el CP no permitió decidir
        if (provNormalizada.contains("capital") || provNormalizada.contains("caba")
                || provNormalizada.contains("ciudad autonoma")) {
            return ZonaEnvio.LOCAL;
        }

        if (provNormalizada.contains("buenos aires") || provNormalizada.contains("bs as")
                || provNormalizada.contains("santa fe") || provNormalizada.contains("cordoba")
                || provNormalizada.contains("entre rios")) {
            return ZonaEnvio.REGIONAL;
        }

        // Resto del país: Tarifa plana nacional
        return ZonaEnvio.NACIONAL;
    }

    private boolean esProvinciaPatagonia(String prov) {
        return prov.contains("tierra del fuego")
                || prov.contains("santa cruz")
                || prov.contains("chubut")
                || prov.contains("rio negro")
                || prov.contains("neuquen")
                || prov.contains("antartida");
    }

    private boolean esProvinciaNacional(String prov) {
        return prov.contains("mendoza")
                || prov.contains("san juan")
                || prov.contains("san luis")
                || prov.contains("salta")
                || prov.contains("jujuy")
                || prov.contains("tucuman")
                || prov.contains("santiago del estero")
                || prov.contains("catamarca")
                || prov.contains("la rioja")
                || prov.contains("chaco")
                || prov.contains("corrientes")
                || prov.contains("misiones")
                || prov.contains("formosa")
                || prov.contains("la pampa");
    }

    private Character extraerPrefijoLetra(String cp) {
        if (cp.length() > 0 && Character.isLetter(cp.charAt(0))) {
            return cp.charAt(0);
        }
        return null;
    }

    private Integer extraerNumeroCp(String cp) {
        Matcher matcher = CP_NUM_PATTERN.matcher(cp);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        String normalizado = Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD);
        return normalizado.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private String generarMensajeZona(ZonaEnvio zona, double peso) {
        String nombreZona;
        switch (zona) {
            case LOCAL:
                nombreZona = "Local (AMBA)";
                break;
            case REGIONAL:
                nombreZona = "Regional (Centro)";
                break;
            case PATAGONIA:
                nombreZona = "Patagonia";
                break;
            case NACIONAL:
            default:
                nombreZona = "Nacional";
                break;
        }
        return String.format("Envío a domicilio - Zona %s (Peso: %.2f kg)", nombreZona, peso);
    }

    @Override
    public String getProviderName() {
        return "PROPIO";
    }

    // Getters y setters para facilitar tests unitarios
    public void setLocalCostoBase(BigDecimal localCostoBase) {
        this.localCostoBase = localCostoBase;
    }

    public void setLocalCostoPorKg(BigDecimal localCostoPorKg) {
        this.localCostoPorKg = localCostoPorKg;
    }

    public void setRegionalCostoBase(BigDecimal regionalCostoBase) {
        this.regionalCostoBase = regionalCostoBase;
    }

    public void setRegionalCostoPorKg(BigDecimal regionalCostoPorKg) {
        this.regionalCostoPorKg = regionalCostoPorKg;
    }

    public void setNacionalCostoBase(BigDecimal nacionalCostoBase) {
        this.nacionalCostoBase = nacionalCostoBase;
    }

    public void setNacionalCostoPorKg(BigDecimal nacionalCostoPorKg) {
        this.nacionalCostoPorKg = nacionalCostoPorKg;
    }

    public void setPatagoniaCostoBase(BigDecimal patagoniaCostoBase) {
        this.patagoniaCostoBase = patagoniaCostoBase;
    }

    public void setPatagoniaCostoPorKg(BigDecimal patagoniaCostoPorKg) {
        this.patagoniaCostoPorKg = patagoniaCostoPorKg;
    }

    public void setPesoBaseIncluido(double pesoBaseIncluido) {
        this.pesoBaseIncluido = pesoBaseIncluido;
    }

    public void setLocalDiasEstimados(String localDiasEstimados) {
        this.localDiasEstimados = localDiasEstimados;
    }

    public void setRegionalDiasEstimados(String regionalDiasEstimados) {
        this.regionalDiasEstimados = regionalDiasEstimados;
    }

    public void setNacionalDiasEstimados(String nacionalDiasEstimados) {
        this.nacionalDiasEstimados = nacionalDiasEstimados;
    }

    public void setPatagoniaDiasEstimados(String patagoniaDiasEstimados) {
        this.patagoniaDiasEstimados = patagoniaDiasEstimados;
    }

}
