package com.example.lasercut.laser_cut_back.domain.cotizacion.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.lasercut.laser_cut_back.domain.cotizacion.dto.CotizacionResponse;
import com.example.lasercut.laser_cut_back.domain.cotizacion.service.CotizacionService;

/**
 * Controlador de cotizaciones
 * 
 * PREPARACIÓN FUTURA:
 * - Cuando se implemente el sistema de pedidos, aquí se vinculará la cotización al usuario
 * - Se agregará parámetro: @Authentication Authentication auth
 * - Se guardará el pedido en la base de datos vinculado al usuario autenticado
 * 
 * INTEGRACIÓN MERCADO PAGO:
 * - Cuando se integre MP, aquí se creará la preferencia de pago después de la cotización
 * - Se agregará endpoint: POST /api/cotizacion/{id}/crear-pago
 * - Se retornará la preferencia_id de MP para redirigir al checkout
 */
@RestController
@RequestMapping("/api")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping(path = "/cotizacion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CotizacionResponse> cotizar(
            @RequestParam MultipartFile archivo,
            @RequestParam double espesor,
            @RequestParam String material,
            @RequestParam int cantidad,
            @RequestParam String unidad
            // TODO: Agregar cuando se implemente autenticación obligatoria:
            // @Authentication Authentication auth
    ) throws IOException {
        CotizacionResponse resp = cotizacionService.calcular(archivo, espesor, material, cantidad, unidad);
        
        // TODO: PREPARACIÓN FUTURA - Vincular cotización al usuario
        // if (auth != null) {
        //     String email = auth.getName();
        //     AppUser usuario = userRepository.findByEmail(email).orElse(null);
        //     if (usuario != null) {
        //         Pedido pedido = new Pedido();
        //         pedido.setUsuario(usuario);
        //         pedido.setCotizacion(resp);
        //         pedidoRepository.save(pedido);
        //     }
        // }
        
        return ResponseEntity.ok(resp);
    }
    
}
