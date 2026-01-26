package com.example.lasercut.laser_cut_back.domain.catalogo.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.lasercut.laser_cut_back.domain.catalogo.dto.EspesorDTO;
import com.example.lasercut.laser_cut_back.domain.catalogo.dto.MaterialDTO;
import com.example.lasercut.laser_cut_back.domain.catalogo.dto.TerminacionDTO;
import com.example.lasercut.laser_cut_back.domain.catalogo.model.Material;
import com.example.lasercut.laser_cut_back.domain.catalogo.repository.MaterialRepository;

@Service
public class CatalogoService {

    private final MaterialRepository materialRepository;

    public CatalogoService(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<MaterialDTO> getCatalogoCompleto() {
        List<Material> materiales = materialRepository.findAllWithRelations();

        return materiales.stream()
            .map(this::mapMaterialToDTO)
            .collect(Collectors.toList());
    }

    private MaterialDTO mapMaterialToDTO(Material material) {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(material.getId());
        dto.setNombre(material.getNombre());
        dto.setTipo(material.getTipo());
        dto.setDensidad(material.getDensidad());
        dto.setPrecioPorKg(material.getPrecioPorKg());

        List<EspesorDTO> espesores = material.getEspesores()
            .stream()
            .map(e -> {
                EspesorDTO edto = new EspesorDTO();
                edto.setId(e.getId());
                edto.setEspesorMm(e.getEspesorMm());
                edto.setEspesorInch(Math.round(e.getEspesorMm() / 25.4 * 1000.0) / 1000.0);
                edto.setPrecioExtraPorM2(e.getPrecioExtraPorM2());

                List<TerminacionDTO> terminaciones = e.getTerminaciones()
                        .stream()
                        .map(t -> {
                            TerminacionDTO tdto = new TerminacionDTO();
                            tdto.setId(t.getId());
                            tdto.setNombre(t.getNombre());
                            tdto.setPrecioExtraPorM2(t.getPrecioExtraPorM2());
                            return tdto;
                        })
                        .collect(Collectors.toList());

                edto.setTerminaciones(terminaciones);
                return edto;
            })
            .collect(Collectors.toList());

        dto.setEspesores(espesores);
        return dto;
    }

}
