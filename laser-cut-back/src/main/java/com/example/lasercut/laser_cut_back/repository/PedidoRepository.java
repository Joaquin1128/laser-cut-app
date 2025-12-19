package com.example.lasercut.laser_cut_back.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lasercut.laser_cut_back.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    List<Pedido> findByUsuarioIdOrderByCreatedAtDesc(Long usuarioId);
    
    List<Pedido> findByUsuarioId(Long usuarioId);

}
