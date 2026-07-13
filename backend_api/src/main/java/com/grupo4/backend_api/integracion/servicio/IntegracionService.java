package com.grupo4.backend_api.integracion.servicio;

import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import com.grupo4.backend_api.facturacion.modelo.FacturaDetalle;
import com.grupo4.backend_api.integracion.dto.MensajeIntegracionDTO;
import com.grupo4.backend_api.integracion.modelo.IntegracionHistorial;
import com.grupo4.backend_api.inventario.modelo.Articulo;
import com.grupo4.backend_api.inventario.modelo.ComprobanteCabecera;
import com.grupo4.backend_api.inventario.modelo.ComprobanteDetalle;
import com.grupo4.backend_api.inventario.modelo.TipoMovimiento;
import com.grupo4.backend_api.inventario.negocio.NegocioComprobante;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSConsumer;
import jakarta.jms.Message;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.TextMessage;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RequestScoped
public class IntegracionService {

    @Inject
    private JMSContext jmsContext;

    @Resource(lookup = "java:global/jms/facturaCreadaQueue")
    private Queue facturaCreadaQueue;

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Inject
    private NegocioComprobante negocioComprobante;

    public List<MensajeIntegracionDTO> listarCola() {
        List<MensajeIntegracionDTO> mensajes = new ArrayList<>();
        try (QueueBrowser browser = jmsContext.createBrowser(facturaCreadaQueue)) {
            Enumeration<?> elementos = browser.getEnumeration();
            while (elementos.hasMoreElements()) {
                Message message = (Message) elementos.nextElement();
                mensajes.add(convertirMensaje(message));
            }
            mensajes.sort((a, b) -> a.getFechaPublicacion().compareTo(b.getFechaPublicacion()));
            return mensajes;
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible consultar la cola JMS.", e);
        }
    }

    public List<MensajeIntegracionDTO> listarHistorial() {
        List<IntegracionHistorial> registros = em.createQuery(
                "SELECT h FROM IntegracionHistorial h ORDER BY h.fechaProcesamiento DESC",
                IntegracionHistorial.class)
                .getResultList();
        if (registros == null) {
            return Collections.emptyList();
        }
        return registros.stream().map(this::convertirHistorial).toList();
    }

    @Transactional
    public MensajeIntegracionDTO importar(String idMensaje) {
        if (idMensaje == null || idMensaje.isBlank()) {
            throw new IllegalArgumentException("El identificador del mensaje es obligatorio.");
        }
        if (yaProcesado(idMensaje)) {
            throw new IllegalStateException("El mensaje ya fue importado anteriormente.");
        }

        String selector = "JMSMessageID = '" + idMensaje.replace("'", "''") + "'";
        try (JMSConsumer consumer = jmsContext.createConsumer(facturaCreadaQueue, selector)) {
            Message recibido = consumer.receive(3000);
            if (recibido == null) {
                throw new NoResultException("El mensaje ya no se encuentra en la cola.");
            }
            if (!(recibido instanceof TextMessage texto)) {
                throw new IllegalArgumentException("La cola contiene un mensaje que no es JSON de texto.");
            }

            Map<String, Object> evento = leerJson(texto.getText());
            Map<String, Object> payload = extraerMapa(evento.get("payload"));
            Integer idFactura = numeroEntero(payload.get("idFactura"), "idFactura");
            FacturaCabecera factura = buscarFactura(idFactura);
            TipoMovimiento tipoEgreso = buscarTipoEgreso();

            ComprobanteCabecera comprobante = construirComprobante(factura, tipoEgreso);
            comprobante = negocioComprobante.crear(comprobante);

            IntegracionHistorial historial = new IntegracionHistorial();
            historial.setIdMensaje(recibido.getJMSMessageID());
            historial.setTipoEvento(texto.getStringProperty("tipoEvento") == null
                    ? String.valueOf(evento.getOrDefault("tipoEvento", "FACTURA_CREADA"))
                    : texto.getStringProperty("tipoEvento"));
            historial.setOrigen(texto.getStringProperty("origen") == null
                    ? String.valueOf(evento.getOrDefault("origen", "FACTURACION"))
                    : texto.getStringProperty("origen"));
            historial.setDestino(texto.getStringProperty("destino") == null
                    ? String.valueOf(evento.getOrDefault("destino", "INVENTARIO"))
                    : texto.getStringProperty("destino"));
            historial.setEstado("PROCESADO");
            historial.setFechaPublicacion(new Date(recibido.getJMSTimestamp()));
            historial.setFechaProcesamiento(new Date());
            historial.setIntentos(obtenerIntentos(recibido));
            historial.setPayloadJson(texto.getText());
            historial.setIdComprobante(comprobante.getIdComprobante());
            historial.setNumeroComprobante(comprobante.getNumeroComprobante());
            em.persist(historial);
            em.flush();

            return convertirHistorial(historial);
        } catch (NoResultException | IllegalArgumentException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("No fue posible importar el mensaje de la cola.", e);
        }
    }

    private ComprobanteCabecera construirComprobante(FacturaCabecera factura, TipoMovimiento tipoEgreso) {
        ComprobanteCabecera comprobante = new ComprobanteCabecera();
        comprobante.setNumeroComprobante("VEN-" + factura.getIdFactura());
        comprobante.setFecha(factura.getFecha());
        comprobante.setIdTipoMovimiento(tipoEgreso);

        List<ComprobanteDetalle> detalles = new ArrayList<>();
        for (FacturaDetalle detalleFactura : factura.getDetalles()) {
            Articulo articulo = em.find(Articulo.class, BigDecimal.valueOf(detalleFactura.getIdArticulo()));
            if (articulo == null) {
                throw new IllegalArgumentException(
                        "No existe el artículo " + detalleFactura.getIdArticulo() + " requerido por la factura.");
            }
            ComprobanteDetalle detalle = new ComprobanteDetalle();
            detalle.setIdArticulo(articulo);
            detalle.setCantidad(BigInteger.valueOf(detalleFactura.getCantidad()));
            detalle.setPrecio(BigDecimal.valueOf(detalleFactura.getPrecio()));
            detalles.add(detalle);
        }
        comprobante.setComprobanteDetalleCollection(detalles);
        return comprobante;
    }

    private FacturaCabecera buscarFactura(Integer idFactura) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT f FROM FacturaCabecera f LEFT JOIN FETCH f.detalles " +
                    "WHERE f.idFactura = :id",
                    FacturaCabecera.class)
                    .setParameter("id", idFactura)
                    .getSingleResult();
        } catch (NoResultException e) {
            throw new IllegalArgumentException("La factura indicada por el mensaje no existe.");
        }
    }

    private TipoMovimiento buscarTipoEgreso() {
        List<TipoMovimiento> tipos = em.createQuery(
                "SELECT t FROM TipoMovimiento t WHERE t.tipo = :tipo ORDER BY t.idTipoMovimiento",
                TipoMovimiento.class)
                .setParameter("tipo", 'E')
                .setMaxResults(1)
                .getResultList();
        if (tipos.isEmpty()) {
            throw new IllegalStateException(
                    "No existe un tipo de movimiento de egreso para importar la factura.");
        }
        return tipos.get(0);
    }

    private boolean yaProcesado(String idMensaje) {
        Long cantidad = em.createQuery(
                "SELECT COUNT(h) FROM IntegracionHistorial h WHERE h.idMensaje = :id",
                Long.class)
                .setParameter("id", idMensaje)
                .getSingleResult();
        return cantidad != null && cantidad > 0;
    }

    private MensajeIntegracionDTO convertirMensaje(Message message) throws Exception {
        MensajeIntegracionDTO dto = new MensajeIntegracionDTO();
        dto.setIdMensaje(message.getJMSMessageID());
        dto.setTipoEvento(valorPropiedad(message, "tipoEvento", "FACTURA_CREADA"));
        dto.setOrigen(valorPropiedad(message, "origen", "FACTURACION"));
        dto.setDestino(valorPropiedad(message, "destino", "INVENTARIO"));
        dto.setEstado("PENDIENTE");
        dto.setFechaPublicacion(Instant.ofEpochMilli(message.getJMSTimestamp()).toString());
        dto.setFechaProcesamiento(null);
        dto.setIntentos(obtenerIntentos(message));
        if (message instanceof TextMessage texto) {
            Map<String, Object> evento = leerJson(texto.getText());
            dto.setPayload(extraerMapa(evento.get("payload")));
        } else {
            dto.setPayload(Collections.emptyMap());
        }
        return dto;
    }

    private MensajeIntegracionDTO convertirHistorial(IntegracionHistorial historial) {
        MensajeIntegracionDTO dto = new MensajeIntegracionDTO();
        dto.setIdMensaje(historial.getIdMensaje());
        dto.setTipoEvento(historial.getTipoEvento());
        dto.setOrigen(historial.getOrigen());
        dto.setDestino(historial.getDestino());
        dto.setEstado(historial.getEstado());
        dto.setFechaPublicacion(historial.getFechaPublicacion().toInstant().toString());
        dto.setFechaProcesamiento(historial.getFechaProcesamiento().toInstant().toString());
        dto.setIntentos(historial.getIntentos());
        Map<String, Object> evento = leerJson(historial.getPayloadJson());
        dto.setPayload(extraerMapa(evento.get("payload")));
        Map<String, Object> comprobante = new LinkedHashMap<>();
        comprobante.put("idComprobante", historial.getIdComprobante());
        comprobante.put("numeroComprobante", historial.getNumeroComprobante());
        dto.setComprobanteGenerado(comprobante);
        return dto;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> leerJson(String json) {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            Object valor = jsonb.fromJson(json, Map.class);
            return valor instanceof Map ? (Map<String, Object>) valor : Collections.emptyMap();
        } catch (Exception e) {
            throw new IllegalArgumentException("El contenido del mensaje no es JSON válido.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extraerMapa(Object valor) {
        if (!(valor instanceof Map)) {
            throw new IllegalArgumentException("El mensaje no contiene un payload válido.");
        }
        return (Map<String, Object>) valor;
    }

    private Integer numeroEntero(Object valor, String campo) {
        if (valor instanceof Number numero) {
            return numero.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(valor));
        } catch (Exception e) {
            throw new IllegalArgumentException("El campo " + campo + " del mensaje no es válido.");
        }
    }

    private int obtenerIntentos(Message message) {
        try {
            int entregas = message.propertyExists("JMSXDeliveryCount")
                    ? message.getIntProperty("JMSXDeliveryCount") : 1;
            return Math.max(entregas, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private String valorPropiedad(Message message, String nombre, String defecto) throws Exception {
        String valor = message.getStringProperty(nombre);
        return valor == null || valor.isBlank() ? defecto : valor;
    }
}
