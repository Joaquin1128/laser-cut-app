package com.example.lasercut.laser_cut_back;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.lasercut.laser_cut_back.domain.archivo.service.DxfStorageService;
import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;
import com.example.lasercut.laser_cut_back.domain.pedido.model.PedidoItem;
import com.example.lasercut.laser_cut_back.domain.pedido.service.OrderEmailService;

public class OrderEmailServiceTest {

    private OrderEmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new OrderEmailService();
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);
        ReflectionTestUtils.setField(emailService, "adminRecipient", "ventas@lasercut.com");
    }

    @Test
    void testEnviarFichaPedidoAdminSimulationDoesNotThrow() {
        Pedido pedido = new Pedido();
        pedido.setId(101L);
        pedido.setStatus(Pedido.OrderStatus.PAID);
        pedido.setBillingName("Empresa Metalúrgica SA");
        pedido.setBillingEmail("contacto@empresa.com");
        pedido.setBillingPhone("1122334455");
        pedido.setBillingType("A");
        pedido.setTaxCondition("RESPONSABLE_INSCRIPTO");
        pedido.setFiscalId("30711111112");
        pedido.setShippingAddressStreet("Av. Corrientes 1234");
        pedido.setShippingAddressCity("CABA");
        pedido.setShippingAddressProvince("Buenos Aires");
        pedido.setShippingAddressPostalCode("1043");
        pedido.setShippingCost(BigDecimal.valueOf(6000));
        pedido.setTotalPrice(BigDecimal.valueOf(25000));

        PedidoItem item = new PedidoItem(pedido, "Acero al Carbono", 2.0, 5, BigDecimal.valueOf(5000), BigDecimal.valueOf(25000));
        item.setId(1L);
        item.setArchivoNombre("engranaje.dxf");
        item.setArchivoId("engranaje_uuid.dxf");

        pedido.setItems(new ArrayList<>());
        pedido.getItems().add(item);

        // No debe lanzar excepción aun sin servidor SMTP
        assertDoesNotThrow(() -> emailService.enviarFichaPedidoAdmin(pedido));
    }

}
