package com.grupo4.backend_api.cobranzas.negocio;

import com.grupo4.backend_api.cobranzas.dto.ReporteEstadoCuentaDTO;
import com.grupo4.backend_api.cobranzas.dto.ReporteMatrizDTO;
import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class NegocioReportes {

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    public List<ReporteEstadoCuentaDTO> generarEstadoCuentaPorFechas(Date inicio, Date fin) {
        validarFechas(inicio, fin);

        List<FacturaCabecera> facturas = em.createQuery(
                "SELECT f FROM FacturaCabecera f JOIN FETCH f.cliente " +
                "WHERE f.fecha BETWEEN :inicio AND :fin " +
                "ORDER BY f.numeroFactura",
                FacturaCabecera.class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();

        List<ReporteEstadoCuentaDTO> resultado = new ArrayList<>();
        for (FacturaCabecera factura : facturas) {
            Double pagado = em.createQuery(
                    "SELECT COALESCE(SUM(p.valor), 0.0) FROM PagoDetalle p " +
                    "WHERE p.factura.idFactura = :idFactura",
                    Double.class)
                    .setParameter("idFactura", factura.getIdFactura())
                    .getSingleResult();

            resultado.add(new ReporteEstadoCuentaDTO(
                    factura.getIdFactura(),
                    factura.getNumeroFactura(),
                    factura.getFecha(),
                    factura.getCliente(),
                    factura.getValorTotal(),
                    pagado));
        }
        return resultado;
    }

    public List<ReporteMatrizDTO> generarMatrizCruzada(Date inicio, Date fin) {
        validarFechas(inicio, fin);

        List<Object[]> filas = em.createQuery(
                "SELECT p.cobrador.nombre, p.formaPago.nombre, SUM(p.valor) " +
                "FROM PagoDetalle p " +
                "WHERE p.fechaPago BETWEEN :inicio AND :fin " +
                "GROUP BY p.cobrador.nombre, p.formaPago.nombre " +
                "ORDER BY p.cobrador.nombre, p.formaPago.nombre",
                Object[].class)
                .setParameter("inicio", inicio)
                .setParameter("fin", fin)
                .getResultList();

        Map<String, ReporteMatrizDTO> matriz = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            String cobrador = String.valueOf(fila[0]);
            String formaPago = String.valueOf(fila[1]);
            Number total = (Number) fila[2];
            matriz.computeIfAbsent(cobrador, ReporteMatrizDTO::new)
                    .agregarValor(formaPago, total == null ? 0.0 : total.doubleValue());
        }
        return new ArrayList<>(matriz.values());
    }

    private void validarFechas(Date inicio, Date fin) {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (inicio.after(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha final.");
        }
    }
}
