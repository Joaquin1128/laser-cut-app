package com.example.lasercut.laser_cut_back.domain.pedido.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    
    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId ORDER BY p.createdAt DESC")
    List<Pedido> findByUsuarioIdOrderByCreatedAtDesc(@Param("usuarioId") Long usuarioId);
    
    List<Pedido> findByUsuarioId(Long usuarioId);
    
    @EntityGraph(attributePaths = {"items"})
    Optional<Pedido> findById(Long id);
    
    @EntityGraph(attributePaths = {"items"})
    Optional<Pedido> findByMercadoPagoPreferenceId(String preferenceId);

    /** Todos los pedidos ordenados por fecha (para admin), con items y usuario cargados. */
    @EntityGraph(attributePaths = {"items", "usuario"})
    @Query("SELECT p FROM Pedido p ORDER BY p.createdAt DESC")
    List<Pedido> findAllOrderByCreatedAtDesc();

}
