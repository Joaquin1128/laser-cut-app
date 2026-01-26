package com.example.lasercut.laser_cut_back.domain.pedido.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.lasercut.laser_cut_back.domain.pedido.model.PedidoItem;

@Repository
public interface PedidoItemRepository extends JpaRepository<PedidoItem, Long> {

    List<PedidoItem> findByPedidoId(Long pedidoId);
    
}
