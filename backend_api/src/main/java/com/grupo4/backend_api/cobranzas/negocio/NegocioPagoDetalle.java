package com.grupo4.backend_api.cobranzas.negocio;

import com.grupo4.backend_api.cobranzas.modelo.Cobrador;
import com.grupo4.backend_api.cobranzas.modelo.FormaPago;
import com.grupo4.backend_api.cobranzas.modelo.PagoDetalle;
import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class NegocioPagoDetalle {

    private static final long LOCK_PAGO = 43001L;

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Transactional
    public int insertar(PagoDetalle pago) {
        validarDatos(pago);
        enlazarReferencias(pago);
        validarSaldo(pago, null);

        pago.setIdPagoDetalle(siguienteIdSeguro());
        em.persist(pago);
        em.flush();
        return 1;
    }

    @Transactional
    public int modificar(PagoDetalle cambios) {
        validarDatos(cambios);

        PagoDetalle existente = em.find(PagoDetalle.class, cambios.getIdPagoDetalle());
        if (existente == null) {
            return 0;
        }

        enlazarReferencias(cambios);
        validarSaldo(cambios, existente.getIdPagoDetalle());

        existente.setFechaPago(cambios.getFechaPago());
        existente.setValor(cambios.getValor());
        existente.setCobrador(cambios.getCobrador());
        existente.setFormaPago(cambios.getFormaPago());
        existente.setFactura(cambios.getFactura());
        em.flush();
        return 1;
    }

    @Transactional
    public int eliminar(Integer idPagoDetalle) {
        PagoDetalle pago = em.find(PagoDetalle.class, idPagoDetalle);
        if (pago == null) {
            return 0;
        }
        em.remove(pago);
        return 1;
    }

    public PagoDetalle buscar(Integer idPagoDetalle) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT p FROM PagoDetalle p " +
                    "JOIN FETCH p.cobrador " +
                    "JOIN FETCH p.formaPago " +
                    "JOIN FETCH p.factura f " +
                    "JOIN FETCH f.cliente " +
                    "JOIN FETCH f.ciudad " +
                    "LEFT JOIN FETCH f.detalles " +
                    "WHERE p.idPagoDetalle = :id",
                    PagoDetalle.class)
                    .setParameter("id", idPagoDetalle)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<PagoDetalle> listarTodos() {
        List<PagoDetalle> pagos = em.createQuery(
                "SELECT DISTINCT p FROM PagoDetalle p " +
                "JOIN FETCH p.cobrador " +
                "JOIN FETCH p.formaPago " +
                "JOIN FETCH p.factura f " +
                "JOIN FETCH f.cliente " +
                "JOIN FETCH f.ciudad " +
                "LEFT JOIN FETCH f.detalles " +
                "ORDER BY p.fechaPago DESC, p.idPagoDetalle DESC",
                PagoDetalle.class)
                .getResultList();
        return pagos == null ? Collections.emptyList() : pagos;
    }

    public List<PagoDetalle> listarPorFactura(Integer idFactura) {
        return em.createQuery(
                "SELECT DISTINCT p FROM PagoDetalle p " +
                "JOIN FETCH p.cobrador " +
                "JOIN FETCH p.formaPago " +
                "JOIN FETCH p.factura f " +
                "JOIN FETCH f.cliente " +
                "JOIN FETCH f.ciudad " +
                "LEFT JOIN FETCH f.detalles " +
                "WHERE f.idFactura = :idFactura " +
                "ORDER BY p.fechaPago DESC, p.idPagoDetalle DESC",
                PagoDetalle.class)
                .setParameter("idFactura", idFactura)
                .getResultList();
    }

    private void validarDatos(PagoDetalle pago) {
        if (pago == null) {
            throw new IllegalArgumentException("Los datos del pago son obligatorios.");
        }
        if (pago.getFechaPago() == null) {
            throw new IllegalArgumentException("La fecha del pago es obligatoria.");
        }
        if (pago.getValor() <= 0) {
            throw new IllegalArgumentException("El valor del pago debe ser mayor que cero.");
        }
        if (pago.getCobrador() == null || pago.getCobrador().getIdCobrador() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cobrador.");
        }
        if (pago.getFormaPago() == null || pago.getFormaPago().getCodigo() == null) {
            throw new IllegalArgumentException("Debe seleccionar una forma de pago.");
        }
        if (pago.getFactura() == null || pago.getFactura().getIdFactura() == null) {
            throw new IllegalArgumentException("Debe seleccionar una factura.");
        }
    }

    private void enlazarReferencias(PagoDetalle pago) {
        Cobrador cobrador = em.find(Cobrador.class, pago.getCobrador().getIdCobrador());
        FormaPago formaPago = em.find(FormaPago.class, pago.getFormaPago().getCodigo());
        FacturaCabecera factura = em.find(FacturaCabecera.class, pago.getFactura().getIdFactura());

        if (cobrador == null) {
            throw new IllegalArgumentException("El cobrador seleccionado no existe.");
        }
        if (formaPago == null) {
            throw new IllegalArgumentException("La forma de pago seleccionada no existe.");
        }
        if (factura == null) {
            throw new IllegalArgumentException("La factura seleccionada no existe.");
        }

        pago.setCobrador(cobrador);
        pago.setFormaPago(formaPago);
        pago.setFactura(factura);
    }

    private void validarSaldo(PagoDetalle pago, Integer idPagoExcluir) {
        Double totalPagado;
        if (idPagoExcluir == null) {
            totalPagado = em.createQuery(
                    "SELECT COALESCE(SUM(p.valor), 0.0) FROM PagoDetalle p " +
                    "WHERE p.factura.idFactura = :idFactura",
                    Double.class)
                    .setParameter("idFactura", pago.getFactura().getIdFactura())
                    .getSingleResult();
        } else {
            totalPagado = em.createQuery(
                    "SELECT COALESCE(SUM(p.valor), 0.0) FROM PagoDetalle p " +
                    "WHERE p.factura.idFactura = :idFactura " +
                    "AND p.idPagoDetalle <> :idPago",
                    Double.class)
                    .setParameter("idFactura", pago.getFactura().getIdFactura())
                    .setParameter("idPago", idPagoExcluir)
                    .getSingleResult();
        }

        double saldo = pago.getFactura().getValorTotal() - (totalPagado == null ? 0.0 : totalPagado);
        if (pago.getValor() > saldo + 0.001) {
            throw new IllegalStateException(
                    String.format("El valor supera el saldo pendiente de $%.2f.", Math.max(0.0, saldo)));
        }
    }

    private Integer siguienteIdSeguro() {
        em.createNativeQuery("SELECT pg_advisory_xact_lock(?1)")
                .setParameter(1, LOCK_PAGO)
                .getSingleResult();
        Number maximo = (Number) em.createNativeQuery(
                "SELECT COALESCE(MAX(ID_PAGO_DET), 0) + 1 FROM PAGO_DETALLE")
                .getSingleResult();
        return maximo.intValue();
    }
}
