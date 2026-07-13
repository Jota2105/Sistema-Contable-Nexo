package com.grupo4.backend_api.cobranzas.dto;

import com.grupo4.backend_api.facturacion.modelo.Cliente;
import java.util.Date;

public class ReporteEstadoCuentaDTO {
    private Integer idFactura;
    private String numeroFactura;
    private Date fecha;
    private Cliente cliente;
    private Double valorFactura;
    private Double totalPagado;
    private Double saldoPorCobrar;
    private String estado;

    public ReporteEstadoCuentaDTO() {
    }

    public ReporteEstadoCuentaDTO(Integer idFactura, String numeroFactura, Date fecha, Cliente cliente,
            Double valorFactura, Double totalPagado) {
        this.idFactura = idFactura;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.cliente = cliente;
        this.valorFactura = valorFactura == null ? 0.0 : valorFactura;
        this.totalPagado = totalPagado == null ? 0.0 : totalPagado;
        this.saldoPorCobrar = Math.max(0.0, this.valorFactura - this.totalPagado);
        this.estado = this.saldoPorCobrar <= 0.001
                ? "PAGADA"
                : this.totalPagado > 0 ? "PARCIAL" : "PENDIENTE";
    }

    public Integer getIdFactura() { return idFactura; }
    public String getNumeroFactura() { return numeroFactura; }
    public Date getFecha() { return fecha; }
    public Cliente getCliente() { return cliente; }
    public Double getValorFactura() { return valorFactura; }
    public Double getTotalPagado() { return totalPagado; }
    public Double getSaldoPorCobrar() { return saldoPorCobrar; }
    public String getEstado() { return estado; }
}
