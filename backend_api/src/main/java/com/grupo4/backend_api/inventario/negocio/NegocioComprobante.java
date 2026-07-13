package com.grupo4.backend_api.inventario.negocio;

import com.grupo4.backend_api.inventario.modelo.Articulo;
import com.grupo4.backend_api.inventario.modelo.ComprobanteCabecera;
import com.grupo4.backend_api.inventario.modelo.ComprobanteDetalle;
import com.grupo4.backend_api.inventario.modelo.TipoMovimiento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class NegocioComprobante {

    private static final long LOCK_CABECERA = 42001L;
    private static final long LOCK_DETALLE = 42002L;

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Transactional
    public ComprobanteCabecera crear(ComprobanteCabecera comprobante) {
        validar(comprobante);
        validarNumeroUnico(comprobante.getNumeroComprobante(), null);

        TipoMovimiento tipo = em.find(TipoMovimiento.class,
                comprobante.getIdTipoMovimiento().getIdTipoMovimiento());
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento seleccionado no existe.");
        }

        comprobante.setIdComprobante(siguienteId("COMPROBANTE_CABECERA", "ID_COMPROBANTE", LOCK_CABECERA));
        comprobante.setNumeroComprobante(comprobante.getNumeroComprobante().trim());
        comprobante.setFecha(comprobante.getFecha() == null ? new Date() : comprobante.getFecha());
        comprobante.setIdTipoMovimiento(tipo);
        comprobante.setComprobanteDetalleCollection(prepararDetalles(
                comprobante,
                comprobante.getComprobanteDetalleCollection(),
                tipo));

        em.persist(comprobante);
        em.flush();
        return comprobante;
    }

    @Transactional
    public ComprobanteCabecera actualizar(BigDecimal id, ComprobanteCabecera cambios) {
        ComprobanteCabecera existente = buscar(id);
        if (existente == null) {
            return null;
        }
        validar(cambios);
        validarNumeroUnico(cambios.getNumeroComprobante(), id);

        TipoMovimiento tipo = em.find(TipoMovimiento.class,
                cambios.getIdTipoMovimiento().getIdTipoMovimiento());
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de movimiento seleccionado no existe.");
        }

        existente.setNumeroComprobante(cambios.getNumeroComprobante().trim());
        existente.setFecha(cambios.getFecha() == null ? existente.getFecha() : cambios.getFecha());
        existente.setIdTipoMovimiento(tipo);
        existente.getComprobanteDetalleCollection().clear();
        em.flush();
        existente.getComprobanteDetalleCollection().addAll(
                prepararDetalles(existente, cambios.getComprobanteDetalleCollection(), tipo));
        em.flush();
        return existente;
    }

    @Transactional
    public boolean eliminar(BigDecimal id) {
        ComprobanteCabecera comprobante = em.find(ComprobanteCabecera.class, id);
        if (comprobante == null) {
            return false;
        }
        em.remove(comprobante);
        return true;
    }

    public ComprobanteCabecera buscar(BigDecimal id) {
        try {
            return em.createQuery(
                    "SELECT DISTINCT c FROM ComprobanteCabecera c " +
                    "LEFT JOIN FETCH c.comprobanteDetalleCollection WHERE c.idComprobante = :id",
                    ComprobanteCabecera.class)
                    .setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<ComprobanteCabecera> listar() {
        return em.createQuery(
                "SELECT DISTINCT c FROM ComprobanteCabecera c " +
                "LEFT JOIN FETCH c.comprobanteDetalleCollection " +
                "ORDER BY c.fecha DESC, c.idComprobante DESC",
                ComprobanteCabecera.class)
                .getResultList();
    }

    public List<Object[]> movimientos(Date inicio, Date fin) {
        return em.createQuery(
                "SELECT d.idArticulo.idArticulo, d.idArticulo.nombre, " +
                "c.idTipoMovimiento.nombre, c.idTipoMovimiento.tipo, SUM(d.cantidad) " +
                "FROM ComprobanteDetalle d JOIN d.idComprobante c " +
                "WHERE c.fecha BETWEEN :inicio AND :fin " +
                "GROUP BY d.idArticulo.idArticulo, d.idArticulo.nombre, " +
                "c.idTipoMovimiento.nombre, c.idTipoMovimiento.tipo " +
                "ORDER BY d.idArticulo.nombre, c.idTipoMovimiento.nombre",
                Object[].class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();
    }

    public int stock(BigDecimal idArticulo) {
        Number valor = (Number) em.createQuery(
                "SELECT COALESCE(SUM(CASE WHEN c.idTipoMovimiento.tipo = 'I' " +
                "THEN d.cantidad ELSE -d.cantidad END), 0) " +
                "FROM ComprobanteDetalle d JOIN d.idComprobante c " +
                "WHERE d.idArticulo.idArticulo = :id")
                .setParameter("id", idArticulo)
                .getSingleResult();
        return valor == null ? 0 : valor.intValue();
    }

    private Collection<ComprobanteDetalle> prepararDetalles(
            ComprobanteCabecera cabecera,
            Collection<ComprobanteDetalle> originales,
            TipoMovimiento tipo) {
        List<ComprobanteDetalle> preparados = new ArrayList<>();
        for (ComprobanteDetalle detalle : originales) {
            if (detalle == null || detalle.getIdArticulo() == null ||
                    detalle.getIdArticulo().getIdArticulo() == null) {
                throw new IllegalArgumentException("Debe seleccionar un artículo en cada detalle.");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigInteger.ZERO) <= 0) {
                throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
            }
            if (detalle.getPrecio() == null || detalle.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo.");
            }

            Articulo articulo = em.find(Articulo.class, detalle.getIdArticulo().getIdArticulo());
            if (articulo == null) {
                throw new IllegalArgumentException("El artículo seleccionado no existe.");
            }
            if (Character.toUpperCase(tipo.getTipo()) == 'E' && stock(articulo.getIdArticulo()) < detalle.getCantidad().intValue()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el artículo " + articulo.getNombre() + ".");
            }

            ComprobanteDetalle preparado = new ComprobanteDetalle();
            preparado.setIdComprobanteDet(siguienteId("COMPROBANTE_DETALLE", "ID_COMPROBANTE_DET", LOCK_DETALLE));
            preparado.setIdComprobante(cabecera);
            preparado.setIdArticulo(articulo);
            preparado.setCantidad(detalle.getCantidad());
            preparado.setPrecio(detalle.getPrecio());
            preparados.add(preparado);
        }
        return preparados;
    }

    private void validar(ComprobanteCabecera comprobante) {
        if (comprobante == null) {
            throw new IllegalArgumentException("Los datos del comprobante son obligatorios.");
        }
        if (comprobante.getNumeroComprobante() == null || comprobante.getNumeroComprobante().trim().isEmpty()) {
            throw new IllegalArgumentException("El número del comprobante es obligatorio.");
        }
        if (comprobante.getIdTipoMovimiento() == null ||
                comprobante.getIdTipoMovimiento().getIdTipoMovimiento() == null) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de movimiento.");
        }
        if (comprobante.getComprobanteDetalleCollection() == null ||
                comprobante.getComprobanteDetalleCollection().isEmpty()) {
            throw new IllegalArgumentException("El comprobante debe contener al menos un detalle.");
        }
    }

    private void validarNumeroUnico(String numero, BigDecimal idExcluir) {
        String jpql = "SELECT COUNT(c) FROM ComprobanteCabecera c " +
                "WHERE LOWER(c.numeroComprobante) = :numero" +
                (idExcluir == null ? "" : " AND c.idComprobante <> :id");
        var consulta = em.createQuery(jpql, Long.class)
                .setParameter("numero", numero.trim().toLowerCase());
        if (idExcluir != null) {
            consulta.setParameter("id", idExcluir);
        }
        if (consulta.getSingleResult() > 0) {
            throw new IllegalStateException("Ya existe un comprobante con ese número.");
        }
    }

    private BigDecimal siguienteId(String tabla, String columna, long lock) {
        em.createNativeQuery("SELECT pg_advisory_xact_lock(?1)")
                .setParameter(1, lock)
                .getSingleResult();
        Number valor = (Number) em.createNativeQuery(
                "SELECT COALESCE(MAX(" + columna + "), 0) + 1 FROM " + tabla)
                .getSingleResult();
        return BigDecimal.valueOf(valor.longValue());
    }
}
