package com.example.lasercut.laser_cut_back.domain.catalogo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.lasercut.laser_cut_back.domain.catalogo.model.Material;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {

    Optional<Material> findByNombreIgnoreCase(String nombre);

    @Query("SELECT m FROM Material m")
    @EntityGraph(attributePaths = {"espesores", "espesores.terminaciones"})
    List<Material> findAllWithRelations();

}
