package com.example.lasercut.laser_cut_back;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteRequest;
import com.example.lasercut.laser_cut_back.domain.shipping.dto.ShippingQuoteResponse;
import com.example.lasercut.laser_cut_back.domain.shipping.provider.CustomShippingProvider;
import com.example.lasercut.laser_cut_back.domain.shipping.service.ShippingService;

public class ShippingServiceTest {

    private CustomShippingProvider customShippingProvider;
    private ShippingService shippingService;

    @BeforeEach
    public void setup() {
        customShippingProvider = new CustomShippingProvider();
        customShippingProvider.setLocalCostoBase(new BigDecimal("6000.00"));
        customShippingProvider.setLocalCostoPorKg(new BigDecimal("500.00"));
        customShippingProvider.setLocalDiasEstimados("2 a 4 días hábiles");

        customShippingProvider.setRegionalCostoBase(new BigDecimal("9000.00"));
        customShippingProvider.setRegionalCostoPorKg(new BigDecimal("800.00"));
        customShippingProvider.setRegionalDiasEstimados("3 a 5 días hábiles");

        customShippingProvider.setNacionalCostoBase(new BigDecimal("12500.00"));
        customShippingProvider.setNacionalCostoPorKg(new BigDecimal("1000.00"));
        customShippingProvider.setNacionalDiasEstimados("4 a 7 días hábiles");

        customShippingProvider.setPatagoniaCostoBase(new BigDecimal("16000.00"));
        customShippingProvider.setPatagoniaCostoPorKg(new BigDecimal("1400.00"));
        customShippingProvider.setPatagoniaDiasEstimados("6 a 10 días hábiles");

        customShippingProvider.setPesoBaseIncluido(2.0);

        shippingService = new ShippingService(customShippingProvider);
    }

    @Test
    public void testZonaLocalCaba() {
        ShippingQuoteRequest request = new ShippingQuoteRequest();
        request.setStreet("Av. Santa Fe 1234");
        request.setCity("CABA");
        request.setPostalCode("1425");
        request.setProvince("Ciudad Autónoma de Buenos Aires");
        request.setTotalWeight(1.5);

        ShippingQuoteResponse response = shippingService.calculateShipping(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.getShippingCost());
        assertEquals("2 a 4 días hábiles", response.getEstimatedDays());
        assertEquals("PROPIO", response.getProvider());
        assertTrue(response.getMessage().contains("Local (AMBA)"));
    }

    @Test
    public void testZonaLocalGbaCpa() {
        ShippingQuoteRequest request = new ShippingQuoteRequest();
        request.setStreet("Av. Maipú 2000");
        request.setCity("Vicente López");
        request.setPostalCode("B1638ABC");
        request.setProvince("Buenos Aires");
        request.setTotalWeight(1.0);

        ShippingQuoteResponse response = shippingService.calculateShipping(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("6000.00"), response.getShippingCost());
        assertTrue(response.getMessage().contains("Local (AMBA)"));
    }

    @Test
    public void testZonaRegionalCordobaYSantaFe() {
        // Córdoba capital (CP 5000)
        ShippingQuoteRequest reqCordoba = new ShippingQuoteRequest();
        reqCordoba.setStreet("Colón 500");
        reqCordoba.setCity("Córdoba");
        reqCordoba.setPostalCode("5000");
        reqCordoba.setProvince("Córdoba");
        reqCordoba.setTotalWeight(2.0);

        ShippingQuoteResponse respCordoba = shippingService.calculateShipping(reqCordoba);
        assertEquals(new BigDecimal("9000.00"), respCordoba.getShippingCost());
        assertEquals("3 a 5 días hábiles", respCordoba.getEstimatedDays());

        // Rosario, Santa Fe (CPA S2000XYZ)
        ShippingQuoteRequest reqRosario = new ShippingQuoteRequest();
        reqRosario.setStreet("Pellegrini 1000");
        reqRosario.setCity("Rosario");
        reqRosario.setPostalCode("S2000XYZ");
        reqRosario.setProvince("Santa Fe");
        reqRosario.setTotalWeight(1.8);

        ShippingQuoteResponse respRosario = shippingService.calculateShipping(reqRosario);
        assertEquals(new BigDecimal("9000.00"), respRosario.getShippingCost());
    }

    @Test
    public void testZonaNacionalAplanadaMendozaYSalta() {
        // Mendoza (CP 5500)
        ShippingQuoteRequest reqMendoza = new ShippingQuoteRequest();
        reqMendoza.setStreet("San Martín 800");
        reqMendoza.setCity("Mendoza");
        reqMendoza.setPostalCode("5500");
        reqMendoza.setProvince("Mendoza");
        reqMendoza.setTotalWeight(2.0);

        ShippingQuoteResponse respMendoza = shippingService.calculateShipping(reqMendoza);

        // Salta (CP 4400) - más del doble de distancia, pero misma tarifa aplanada
        ShippingQuoteRequest reqSalta = new ShippingQuoteRequest();
        reqSalta.setStreet("Belgrano 300");
        reqSalta.setCity("Salta");
        reqSalta.setPostalCode("4400");
        reqSalta.setProvince("Salta");
        reqSalta.setTotalWeight(2.0);

        ShippingQuoteResponse respSalta = shippingService.calculateShipping(reqSalta);

        assertEquals(new BigDecimal("12500.00"), respMendoza.getShippingCost());
        assertEquals(new BigDecimal("12500.00"), respSalta.getShippingCost());
        assertEquals(respMendoza.getShippingCost(), respSalta.getShippingCost(), "La tarifa nacional debe estar aplanada");
        assertEquals("4 a 7 días hábiles", respMendoza.getEstimatedDays());
    }

    @Test
    public void testZonaPatagoniaUshuaia() {
        ShippingQuoteRequest request = new ShippingQuoteRequest();
        request.setStreet("San Martín 100");
        request.setCity("Ushuaia");
        request.setPostalCode("9410");
        request.setProvince("Tierra del Fuego");
        request.setTotalWeight(2.0);

        ShippingQuoteResponse response = shippingService.calculateShipping(request);

        assertEquals(new BigDecimal("16000.00"), response.getShippingCost());
        assertEquals("6 a 10 días hábiles", response.getEstimatedDays());
        assertTrue(response.getMessage().contains("Patagonia"));
    }

    @Test
    public void testRecargoPorExcesoDePeso() {
        // En Zona Nacional: base $12500 hasta 2.0 kg. Extra: $1000 por kg.
        // Paquete de 5.5 kg -> exceso = 3.5 kg -> adicional = 3.5 * 1000 = $3500.
        // Total esperado = $16000.00
        ShippingQuoteRequest request = new ShippingQuoteRequest();
        request.setStreet("Mitre 500");
        request.setCity("Tucumán");
        request.setPostalCode("4000");
        request.setProvince("Tucumán");
        request.setTotalWeight(5.5);

        ShippingQuoteResponse response = shippingService.calculateShipping(request);

        assertEquals(new BigDecimal("16000.00"), response.getShippingCost());
    }

    @Test
    public void testFallbackPorNombreDeProvincia() {
        // CP no numérico o extraño, pero provincia 'Santa Cruz' -> Patagonia
        ShippingQuoteRequest reqPatagonia = new ShippingQuoteRequest();
        reqPatagonia.setStreet("Calle 1");
        reqPatagonia.setCity("Río Gallegos");
        reqPatagonia.setPostalCode("SIN-CP");
        reqPatagonia.setProvince("Santa Cruz");
        reqPatagonia.setTotalWeight(1.0);

        ShippingQuoteResponse respPatagonia = shippingService.calculateShipping(reqPatagonia);
        assertEquals(new BigDecimal("16000.00"), respPatagonia.getShippingCost());

        // CP vacío, provincia 'Capital Federal' -> Local
        ShippingQuoteRequest reqLocal = new ShippingQuoteRequest();
        reqLocal.setStreet("Corrientes 500");
        reqLocal.setCity("Buenos Aires");
        reqLocal.setPostalCode("");
        reqLocal.setProvince("CABA");
        reqLocal.setTotalWeight(1.0);

        ShippingQuoteResponse respLocal = shippingService.calculateShipping(reqLocal);
        assertEquals(new BigDecimal("6000.00"), respLocal.getShippingCost());
    }

    @Test
    public void testRetiroEnFabrica() {
        ShippingQuoteResponse pickup = shippingService.getPickupCost();
        assertEquals(BigDecimal.ZERO, pickup.getShippingCost());
        assertEquals("PICKUP", pickup.getProvider());
        assertEquals("0 días", pickup.getEstimatedDays());
    }

}
