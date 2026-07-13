package com.grupo4.backend_api.facturacion.negocio;

import com.grupo4.backend_api.facturacion.modelo.CiudadEntrega;
import com.grupo4.backend_api.facturacion.modelo.Cliente;
import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import com.grupo4.backend_api.facturacion.modelo.FacturaDetalle;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class NegocioFactura {

    private static final long LOCK_FACTURA = 41001L;
    private static final long LOCK_DETALLE = 41002L;

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Transactional
    public int insertar(FacturaCabecera factura) {
        validarFactura(factura);

        Cliente cliente = em.find(Cliente.class, factura.getCliente().getIdCliente());
        CiudadEntrega ciudad = em.find(CiudadEntrega.class, factura.getCiudad().getIdCiudad());
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente seleccionado no existe.");
        }
        if (ciudad == null) {
            throw new IllegalArgumentException("La ciudad seleccionada no existe.");
        }

        Long repetidas = em.createQuery(
                "SELECT COUNT(f) FROM FacturaCabecera f WHERE LOWER(f.numeroFactura) = :numero",
                Long.class)
                .setParameter("numero", factura.getNumeroFactura().trim().toLowerCase())
                .getSingleResult();
        if (repetidas > 0) {
            throw new IllegalStateException("Ya existe una factura con ese número.");
        }

        factura.setNumeroFactura(factura.getNumeroFactura().trim());
        factura.setCliente(cliente);
        factura.setCiudad(ciudad);
        factura.setIdFactura(siguienteIdSeguro("FACTURA_CABECERA", "ID_FACTURA", LOCK_FACTURA));

        double total = 0.0;
        for (FacturaDetalle detalle : factura.getDetalles()) {
            validarDetalle(detalle);
            validarStockDisponible(BigDecimal.valueOf(detalle.getIdArticulo()), detalle.getCantidad());
            detalle.setIdFacturaDet(siguienteIdSeguro("FACTURA_DETALLE", "ID_FACTURA_DET", LOCK_DETALLE));
            detalle.setFactura(factura);
            total += detalle.getCantidad() * detalle.getPrecio();
        }
        factura.setValorTotal(total);
        em.persist(factura);
        em.flush();
        return 1;
    }

    @Transactional
    public int eliminar(Integer idFactura) {
        FacturaCabecera factura = em.find(FacturaCabecera.class, idFactura);
        if (factura == null) {
            return 0;
        }
        em.remove(factura);
        return 1;
    }

    @Transactional
    public int eliminarDetalle(Integer idDetalle) {
        FacturaDetalle detalle = em.find(FacturaDetalle.class, idDetalle);
        if (detalle == null) {
            return 0;
        }
        FacturaCabecera factura = detalle.getFactura();
        em.remove(detalle);
        em.flush();
        recalcularTotal(factura.getIdFactura());
        return 1;
    }

    @Transactional
    public int modificarDetalle(FacturaDetalle cambios) {
        validarDetalle(cambios);
        FacturaDetalle detalle = em.find(FacturaDetalle.class, cambios.getIdFacturaDet());
        if (detalle == null) {
            return 0;
        }
        detalle.setCantidad(cambios.getCantidad());
        detalle.setPrecio(cambios.getPrecio());
        em.flush();
        recalcularTotal(detalle.getFactura().getIdFactura());
        return 1;
    }

    public FacturaCabecera buscar(Integer idFactura) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT f FROM FacturaCabecera f " +
                    "LEFT JOIN FETCH f.detalles WHERE f.idFactura = :id",
                    FacturaCabecera.class)
                    .setParameter("id", idFactura)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<FacturaCabecera> listarTodos() {
        List<FacturaCabecera> facturas = em.createQuery(
                "SELECT DISTINCT f FROM FacturaCabecera f " +
                "LEFT JOIN FETCH f.detalles ORDER BY f.fecha DESC, f.idFactura DESC",
                FacturaCabecera.class)
                .getResultList();
        return facturas == null ? Collections.emptyList() : facturas;
    }

    public List<Object[]> reporteVentasPorCiudad() {
        return em.createQuery(
                "SELECT c.nombre, COALESCE(SUM(f.valorTotal), 0) " +
                "FROM FacturaCabecera f JOIN f.ciudad c " +
                "GROUP BY c.nombre ORDER BY SUM(f.valorTotal) DESC",
                Object[].class)
                .getResultList();
    }

    public List<Object[]> reporteMatrizVentas() {
        return em.createQuery(
                "SELECT d.idArticulo, f.cliente.idCliente, " +
                "COALESCE(SUM(d.cantidad * d.precio), 0) " +
                "FROM FacturaDetalle d JOIN d.factura f " +
                "GROUP BY d.idArticulo, f.cliente.idCliente " +
                "ORDER BY d.idArticulo, f.cliente.idCliente",
                Object[].class)
                .getResultList();
    }

    public void validarStockDisponible(BigDecimal idArticulo, int cantidadSolicitada) {
        Number stock = (Number) em.createNativeQuery(
                "SELECT COALESCE(SUM(CASE WHEN tm.tipo = 'I' THEN cd.cantidad ELSE -cd.cantidad END), 0) " +
                "FROM comprobante_detalle cd " +
                "JOIN comprobante_cabecera cc ON cc.id_comprobante = cd.id_comprobante " +
                "JOIN tipo_movimiento tm ON tm.id_tipo_movimiento = cc.id_tipo_movimiento " +
                "WHERE cd.id_articulo = ?1")
                .setParameter(1, idArticulo)
                .getSingleResult();
        if (stock.intValue() < cantidadSolicitada) {
            throw new IllegalArgumentException(
                    "Saldo insuficiente para el artículo " + idArticulo + ". Stock real: " + stock.intValue());
        }
    }

    private void validarFactura(FacturaCabecera factura) {
        if (factura == null) {
            throw new IllegalArgumentException("Los datos de la factura son obligatorios.");
        }
        if (factura.getNumeroFactura() == null || factura.getNumeroFactura().trim().isEmpty()) {
            throw new IllegalArgumentException("El número de factura es obligatorio.");
        }
        if (factura.getFecha() == null) {
            throw new IllegalArgumentException("La fecha de la factura es obligatoria.");
        }
        if (factura.getCliente() == null || factura.getCliente().getIdCliente() == null) {
            throw new IllegalArgumentException("Debe seleccionar un cliente.");
        }
        if (factura.getCiudad() == null || factura.getCiudad().getIdCiudad() == null) {
            throw new IllegalArgumentException("Debe seleccionar una ciudad.");
        }
        if (factura.getDetalles() == null || factura.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La factura debe contener al menos un detalle.");
        }
    }

    private void validarDetalle(FacturaDetalle detalle) {
        if (detalle == null || detalle.getIdArticulo() == null) {
            throw new IllegalArgumentException("Debe seleccionar un artículo en cada detalle.");
        }
        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (detalle.getPrecio() == null || detalle.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
    }

    private Integer siguienteIdSeguro(String tabla, String columna, long llave) {
        em.createNativeQuery("SELECT pg_advisory_xact_lock(?1)")
                .setParameter(1, llave)
                .getSingleResult();
        Number maximo = (Number) em.createNativeQuery(
                "SELECT COALESCE(MAX(" + columna + "), 0) + 1 FROM " + tabla)
                .getSingleResult();
        return maximo.intValue();
    }

    private void recalcularTotal(Integer idFactura) {
        Double total = em.createQuery(
                "SELECT COALESCE(SUM(d.cantidad * d.precio), 0) " +
                "FROM FacturaDetalle d WHERE d.factura.idFactura = :id",
                Double.class)
                .setParameter("id", idFactura)
                .getSingleResult();
        FacturaCabecera factura = em.find(FacturaCabecera.class, idFactura);
        if (factura != null) {
            factura.setValorTotal(total == null ? 0.0 : total);
        }
    }
}
