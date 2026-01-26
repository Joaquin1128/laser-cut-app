package com.example.lasercut.laser_cut_back.domain.catalogo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.lasercut.laser_cut_back.domain.catalogo.dto.MaterialDTO;
import com.example.lasercut.laser_cut_back.domain.catalogo.service.CatalogoService;

@RestController
@RequestMapping("/api/catalogo")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    public List<MaterialDTO> getCatalogoCompleto() {
        return catalogoService.getCatalogoCompleto();
    }
    
}
