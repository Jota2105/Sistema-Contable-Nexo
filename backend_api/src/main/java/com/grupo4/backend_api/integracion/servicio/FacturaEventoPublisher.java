package com.grupo4.backend_api.integracion.servicio;

import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import com.grupo4.backend_api.facturacion.modelo.FacturaDetalle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
public class FacturaEventoPublisher {

    @Inject
    private JmsClientFactory jmsClientFactory;

    public void publicar(FacturaCabecera factura) {
        Map<String, Object> evento = new LinkedHashMap<>();
        evento.put("tipoEvento", "FACTURA_CREADA");
        evento.put("origen", "FACTURACION");
        evento.put("destino", "INVENTARIO");
        evento.put("estado", "PENDIENTE");
        evento.put("fechaPublicacion", Instant.now().toString());
        evento.put("intentos", 0);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("idFactura", factura.getIdFactura());
        payload.put("numeroFactura", factura.getNumeroFactura());
        payload.put("fecha", factura.getFecha());
        payload.put("valorTotal", factura.getValorTotal());
        payload.put("detalles", factura.getDetalles().stream().map(this::detalleComoMapa).toList());
        evento.put("payload", payload);

        Queue queue = jmsClientFactory.facturaCreadaQueue();
        try (Jsonb jsonb = JsonbBuilder.create();
             JMSContext jmsContext = jmsClientFactory.crearContexto(JMSContext.AUTO_ACKNOWLEDGE)) {
            String json = jsonb.toJson(evento);
            jmsContext.createProducer()
                    .setProperty("tipoEvento", "FACTURA_CREADA")
                    .setProperty("origen", "FACTURACION")
                    .setProperty("destino", "INVENTARIO")
                    .send(queue, json);
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible publicar la factura en la cola JMS.", e);
        }
    }

    private Map<String, Object> detalleComoMapa(FacturaDetalle detalle) {
        Map<String, Object> fila = new LinkedHashMap<>();
        fila.put("idArticulo", detalle.getIdArticulo());
        fila.put("nombreArticulo", detalle.getNombreArticulo());
        fila.put("cantidad", detalle.getCantidad());
        fila.put("precio", detalle.getPrecio());
        return fila;
    }
}
