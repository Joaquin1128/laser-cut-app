package com.example.lasercut.laser_cut_back;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.example.lasercut.laser_cut_back.domain.auth.model.AppUser;
import com.example.lasercut.laser_cut_back.domain.auth.repository.UserRepository;
import com.example.lasercut.laser_cut_back.domain.pedido.dto.PedidoWithCustomerResponse;
import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;
import com.example.lasercut.laser_cut_back.domain.pedido.repository.PedidoRepository;
import com.example.lasercut.laser_cut_back.domain.pedido.service.PedidoService;
import com.example.lasercut.laser_cut_back.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class PedidoStateMachineTest {

    private PedidoService pedidoService;
    private PedidoRepository pedidoRepository;
    private UserRepository userRepository;
    private Pedido pedido;
    private AppUser usuario;

    @BeforeEach
    public void setUp() {
        pedidoRepository = Mockito.mock(PedidoRepository.class);
        userRepository = Mockito.mock(UserRepository.class);

        pedidoService = new PedidoService();
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoService, "pedidoRepository", pedidoRepository);
        org.springframework.test.util.ReflectionTestUtils.setField(pedidoService, "userRepository", userRepository);

        usuario = new AppUser();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan@example.com");

        pedido = new Pedido(usuario, new BigDecimal("15000.00"));
        pedido.setStatus(Pedido.OrderStatus.PENDING_PAYMENT);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @DisplayName("Debe confirmar pago por transferencia desde PENDING_PAYMENT a PAID")
    public void testConfirmarPagoTransferencia() {
        PedidoWithCustomerResponse resp = pedidoService.cambiarEstadoAdmin(1L, "PAID", "TRANSFERENCIA", "Transferencia recibida en Santander");

        assertNotNull(resp);
        assertEquals("PAID", resp.getStatus());
        assertEquals("APPROVED", resp.getPaymentStatus());
        assertEquals("TRANSFERENCIA", resp.getPaymentMethod());
        assertEquals("Juan Pérez", resp.getCustomerNombre());
    }

    @Test
    @DisplayName("Debe permitir avanzar de PAID a EN_PROCESO y luego a FINALIZADO")
    public void testFlujoFabricacionYFinalizado() {
        pedido.setStatus(Pedido.OrderStatus.PAID);
        pedido.setPaymentStatus(Pedido.PaymentStatus.APPROVED);

        // 1. Iniciar fabricación
        PedidoWithCustomerResponse respProceso = pedidoService.cambiarEstadoAdmin(1L, "EN_PROCESO", null, "Ingresó a corte láser");
        assertEquals("EN_PROCESO", respProceso.getStatus());
        assertEquals("APPROVED", respProceso.getPaymentStatus());

        // 2. Finalizar pedido
        PedidoWithCustomerResponse respFinalizado = pedidoService.cambiarEstadoAdmin(1L, "FINALIZADO", null, "Piezas cortadas y entregadas");
        assertEquals("FINALIZADO", respFinalizado.getStatus());
    }

    @Test
    @DisplayName("Debe rechazar pasar a EN_PROCESO o FINALIZADO sin haber confirmado el pago previamente")
    public void testRechazarSaltoSinPago() {
        pedido.setStatus(Pedido.OrderStatus.PENDING_PAYMENT);

        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> {
            pedidoService.cambiarEstadoAdmin(1L, "EN_PROCESO", null, null);
        });
        assertTrue(ex1.getMessage().contains("sin haber confirmado el pago primero"));

        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> {
            pedidoService.cambiarEstadoAdmin(1L, "FINALIZADO", null, null);
        });
        assertTrue(ex2.getMessage().contains("sin haber confirmado el pago primero"));
    }

    @Test
    @DisplayName("Debe rechazar modificaciones en pedidos que ya están FINALIZADOS o CANCELADOS")
    public void testRechazarModificacionEnEstadosFinales() {
        pedido.setStatus(Pedido.OrderStatus.FINALIZADO);

        assertThrows(BadRequestException.class, () -> {
            pedidoService.cambiarEstadoAdmin(1L, "PAID", null, null);
        });

        pedido.setStatus(Pedido.OrderStatus.CANCELADO);

        assertThrows(BadRequestException.class, () -> {
            pedidoService.cambiarEstadoAdmin(1L, "EN_PROCESO", null, null);
        });
    }

    @Test
    @DisplayName("Debe permitir cancelar un pedido pendiente o pagado")
    public void testCancelarPedido() {
        pedido.setStatus(Pedido.OrderStatus.PENDING_PAYMENT);
        PedidoWithCustomerResponse respCancelado = pedidoService.cambiarEstadoAdmin(1L, "CANCELADO", null, "Cliente desistió");

        assertEquals("CANCELADO", respCancelado.getStatus());
        assertEquals("CANCELLED", respCancelado.getPaymentStatus());
    }

}

