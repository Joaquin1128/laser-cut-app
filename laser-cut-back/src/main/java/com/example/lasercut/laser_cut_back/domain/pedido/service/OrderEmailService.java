package com.example.lasercut.laser_cut_back.domain.pedido.service;

import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.lasercut.laser_cut_back.domain.archivo.service.DxfStorageService;
import com.example.lasercut.laser_cut_back.domain.pedido.model.Pedido;
import com.example.lasercut.laser_cut_back.domain.pedido.model.PedidoItem;

import jakarta.mail.internet.MimeMessage;

@Service
public class OrderEmailService {

    private static final Logger logger = LoggerFactory.getLogger(OrderEmailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private DxfStorageService dxfStorageService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.admin-recipient:ventas@lasercut.com}")
    private String adminRecipient;

    @Value("${spring.mail.username:no-reply@lasercut.com}")
    private String fromEmail;

    /**
     * Envía la ficha completa del pedido por email con los archivos DXF adjuntos.
     * Diseñado para no bloquear la petición HTTP y tolerar ausencia de SMTP en desarrollo.
     */
    @Async
    public void enviarFichaPedidoAdmin(Pedido pedido) {
        if (pedido == null) return;

        String asunto = String.format("[PEDIDO #%d] Ficha de Pedido y Facturación - %s",
                pedido.getId(),
                pedido.getBillingName() != null ? pedido.getBillingName() : "Cliente");

        String cuerpoHtml = construirHtmlFichaPedido(pedido);

        if (!mailEnabled || mailSender == null) {
            logger.info("================================================================================");
            logger.info("SIMULACIÓN DE ENVÍO DE EMAIL DE FICHA DE PEDIDO (mailEnabled={})", mailEnabled);
            logger.info("Destinatario: {}", adminRecipient);
            logger.info("Asunto: {}", asunto);
            logger.info("Facturación: Tipo={}, CUIT/DNI={}, Razón Social={}",
                    pedido.getBillingType(), pedido.getFiscalId(), pedido.getBillingName());
            logger.info("Envío: {}, {}, {}",
                    pedido.getShippingAddressStreet(), pedido.getShippingAddressCity(), pedido.getShippingAddressProvince());
            logger.info("Total: ${}", pedido.getTotalWithShipping());
            logger.info("Adjuntos DXF pendientes: {} items", pedido.getItems() != null ? pedido.getItems().size() : 0);
            logger.info("================================================================================");
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(adminRecipient);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);

            // Adjuntar archivos DXF de cada item
            if (pedido.getItems() != null) {
                for (PedidoItem item : pedido.getItems()) {
                    String archivoId = item.getArchivoId();
                    if (archivoId != null && !archivoId.isBlank()) {
                        File dxfFile = dxfStorageService.loadFile(archivoId);
                        if (dxfFile != null && dxfFile.exists()) {
                            String attachmentName = item.getArchivoNombre() != null && !item.getArchivoNombre().isBlank()
                                    ? item.getArchivoNombre()
                                    : "item_" + item.getId() + ".dxf";
                            helper.addAttachment(attachmentName, dxfFile);
                            logger.info("Adjuntado archivo DXF: {} para item #{}", attachmentName, item.getId());
                        }
                    }
                }
            }

            mailSender.send(mimeMessage);
            logger.info("Email de ficha de pedido #{} enviado con éxito a {}", pedido.getId(), adminRecipient);

        } catch (Exception e) {
            logger.error("Error al enviar el email de la ficha del pedido #{}: {}", pedido.getId(), e.getMessage(), e);
        }
    }

    private String construirHtmlFichaPedido(Pedido pedido) {
        StringBuilder sb = new StringBuilder();
        String fecha = pedido.getCreatedAt() != null ? pedido.getCreatedAt().format(DATE_FORMATTER) : "--";

        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><style>")
          .append("body { font-family: Arial, sans-serif; color: #333; line-height: 1.5; background-color: #f9f9f9; padding: 20px; }")
          .append(".container { max-width: 650px; background: #ffffff; border-radius: 8px; padding: 25px; border: 1px solid #e0e0e0; margin: 0 auto; }")
          .append(".header { border-bottom: 2px solid #2563eb; padding-bottom: 12px; margin-bottom: 20px; }")
          .append(".title { color: #1e293b; font-size: 20px; margin: 0; }")
          .append(".badge-tipo { display: inline-block; padding: 4px 10px; border-radius: 4px; font-weight: bold; background: #e0e7ff; color: #3730a3; margin-left: 8px; }")
          .append(".section { margin-bottom: 22px; }")
          .append(".section-title { font-size: 15px; color: #2563eb; font-weight: bold; margin-bottom: 8px; border-bottom: 1px solid #e2e8f0; padding-bottom: 4px; text-transform: uppercase; }")
          .append(".data-table { width: 100%; border-collapse: collapse; font-size: 14px; }")
          .append(".data-table td { padding: 6px 4px; vertical-align: top; }")
          .append(".data-table td.label { width: 35%; color: #64748b; font-weight: bold; }")
          .append(".data-table td.value { color: #1e293b; }")
          .append(".items-table { width: 100%; border-collapse: collapse; font-size: 13px; margin-top: 8px; }")
          .append(".items-table th { background: #f1f5f9; color: #475569; padding: 8px; text-align: left; border-bottom: 1px solid #cbd5e1; }")
          .append(".items-table td { padding: 8px; border-bottom: 1px solid #e2e8f0; }")
          .append(".totals-table { width: 100%; border-collapse: collapse; margin-top: 15px; font-size: 14px; }")
          .append(".totals-table td { padding: 6px; text-align: right; }")
          .append(".totals-table .total-final { font-size: 18px; font-weight: bold; color: #047857; }")
          .append(".alert-box { background: #fef3c7; border: 1px solid #f59e0b; color: #92400e; padding: 12px; border-radius: 6px; font-size: 13px; margin-top: 20px; }")
          .append("</style></head><body>");

        sb.append("<div class='container'>");
        sb.append("<div class='header'>")
          .append("<h1 class='title'>Ficha de Pedido #").append(pedido.getId())
          .append("<span class='badge-tipo'>Factura ").append(pedido.getBillingType() != null ? pedido.getBillingType() : "B").append("</span></h1>")
          .append("<p style='color: #64748b; margin: 4px 0 0 0; font-size: 13px;'>Fecha: ").append(fecha).append(" | Estado: <strong>").append(pedido.getStatus()).append("</strong></p>")
          .append("</div>");

        // Datos Fiscales
        sb.append("<div class='section'>");
        sb.append("<div class='section-title'>📋 Datos de Facturación (Para emitir en AFIP)</div>");
        sb.append("<table class='data-table'>");
        sb.append("<tr><td class='label'>Tipo Comprobante:</td><td class='value'><strong>Factura ").append(pedido.getBillingType() != null ? pedido.getBillingType() : "B").append("</strong></td></tr>");
        sb.append("<tr><td class='label'>Condición Fiscal:</td><td class='value'>").append(formatearCondicionFiscal(pedido.getTaxCondition())).append("</td></tr>");
        sb.append("<tr><td class='label'>CUIT / DNI:</td><td class='value'><strong style='font-size: 15px;'>").append(pedido.getFiscalId() != null ? pedido.getFiscalId() : "--").append("</strong></td></tr>");
        sb.append("<tr><td class='label'>Razón Social / Nombre:</td><td class='value'>").append(pedido.getBillingName() != null ? pedido.getBillingName() : "--").append("</td></tr>");
        sb.append("<tr><td class='label'>Email de contacto:</td><td class='value'>").append(pedido.getBillingEmail() != null ? pedido.getBillingEmail() : "--").append("</td></tr>");
        sb.append("<tr><td class='label'>Teléfono:</td><td class='value'>").append(pedido.getBillingPhone() != null ? pedido.getBillingPhone() : "--").append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        // Dirección de Envío
        sb.append("<div class='section'>");
        sb.append("<div class='section-title'>📦 Dirección de Envío</div>");
        sb.append("<table class='data-table'>");
        sb.append("<tr><td class='label'>Calle y Número:</td><td class='value'>").append(pedido.getShippingAddressStreet() != null ? pedido.getShippingAddressStreet() : "--");
        if (pedido.getShippingAddressUnit() != null && !pedido.getShippingAddressUnit().isBlank()) {
            sb.append(" (").append(pedido.getShippingAddressUnit()).append(")");
        }
        sb.append("</td></tr>");
        sb.append("<tr><td class='label'>Ciudad / Localidad:</td><td class='value'>").append(pedido.getShippingAddressCity() != null ? pedido.getShippingAddressCity() : "--").append("</td></tr>");
        sb.append("<tr><td class='label'>Código Postal:</td><td class='value'>").append(pedido.getShippingAddressPostalCode() != null ? pedido.getShippingAddressPostalCode() : "--").append("</td></tr>");
        sb.append("<tr><td class='label'>Provincia:</td><td class='value'>").append(pedido.getShippingAddressProvince() != null ? pedido.getShippingAddressProvince() : "--").append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        // Items del Pedido
        sb.append("<div class='section'>");
        sb.append("<div class='section-title'>⚙️ Piezas a Cortar (Archivos DXF adjuntos)</div>");
        sb.append("<table class='items-table'>");
        sb.append("<thead><tr><th>Pieza / Archivo</th><th>Material</th><th>Espesor</th><th>Cant.</th><th>Subtotal</th></tr></thead><tbody>");

        List<PedidoItem> items = pedido.getItems();
        if (items != null && !items.isEmpty()) {
            for (PedidoItem item : items) {
                String nombrePieza = item.getArchivoNombre() != null ? item.getArchivoNombre() : "Pieza #" + item.getId();
                sb.append("<tr>")
                  .append("<td><strong>").append(nombrePieza).append("</strong></td>")
                  .append("<td>").append(item.getMaterial() != null ? item.getMaterial() : "--").append("</td>")
                  .append("<td>").append(item.getThickness() != null ? item.getThickness() : "--").append(" mm</td>")
                  .append("<td>").append(item.getQuantity() != null ? item.getQuantity() : 1).append("</td>")
                  .append("<td>$").append(item.getTotalPrice() != null ? item.getTotalPrice().setScale(2).toString() : "0.00").append("</td>")
                  .append("</tr>");
            }
        } else {
            sb.append("<tr><td colspan='5'>Sin items registrados.</td></tr>");
        }
        sb.append("</tbody></table>");
        sb.append("</div>");

        // Desglose de Pago
        sb.append("<div class='section'>");
        sb.append("<div class='section-title'>💰 Desglose Económico</div>");
        sb.append("<table class='totals-table'>");
        sb.append("<tr><td>Subtotal Piezas:</td><td style='width: 120px;'><strong>$").append(pedido.getTotalPrice() != null ? pedido.getTotalPrice().setScale(2).toString() : "0.00").append("</strong></td></tr>");
        BigDecimal envio = pedido.getShippingCost() != null ? pedido.getShippingCost() : BigDecimal.ZERO;
        sb.append("<tr><td>Costo de Envío:</td><td><strong>$").append(envio.setScale(2).toString()).append("</strong></td></tr>");
        sb.append("<tr class='total-final'><td>Total Pagado:</td><td>$").append(pedido.getTotalWithShipping() != null ? pedido.getTotalWithShipping().setScale(2).toString() : "0.00").append("</td></tr>");
        sb.append("<tr><td>Método de Pago:</td><td>").append(pedido.getPaymentMethod() != null ? pedido.getPaymentMethod() : "MERCADO_PAGO").append("</td></tr>");
        sb.append("<tr><td>Estado del Pago:</td><td>").append(pedido.getPaymentStatus() != null ? pedido.getPaymentStatus().name() : "APPROVED").append("</td></tr>");
        sb.append("</table>");
        sb.append("</div>");

        sb.append("<div class='alert-box'>");
        sb.append("ℹ️ <strong>Nota de Producción:</strong> Los archivos DXF originales listados arriba vienen adjuntos a este mensaje de correo electrónico listos para descargar y cargar a la máquina de corte.");
        sb.append("</div>");

        sb.append("</div></body></html>");
        return sb.toString();
    }

    private String formatearCondicionFiscal(String condicion) {
        if (condicion == null) return "Consumidor Final";
        switch (condicion.toUpperCase()) {
            case "RESPONSABLE_INSCRIPTO":
                return "Responsable Inscripto";
            case "MONOTRIBUTO":
                return "Monotributo";
            case "EXENTO":
                return "Exento";
            case "CONSUMIDOR_FINAL":
            default:
                return "Consumidor Final";
        }
    }

}
