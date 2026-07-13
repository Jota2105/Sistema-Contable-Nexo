package com.grupo4.backend_api.inventario.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "COMPROBANTE_CABECERA")
@NamedQueries({
    @NamedQuery(name = "ComprobanteCabecera.findAll", query = "SELECT c FROM ComprobanteCabecera c"),
    @NamedQuery(name = "ComprobanteCabecera.findByIdComprobante", query = "SELECT c FROM ComprobanteCabecera c WHERE c.idComprobante = :idComprobante"),
    @NamedQuery(name = "ComprobanteCabecera.findByNumeroComprobante", query = "SELECT c FROM ComprobanteCabecera c WHERE c.numeroComprobante = :numeroComprobante")
})
public class ComprobanteCabecera implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ID_COMPROBANTE")
    private BigDecimal idComprobante;

    @Basic(optional = false)
    @Column(name = "NUMERO_COMPROBANTE", unique = true, nullable = false)
    private String numeroComprobante;

    @Basic(optional = false)
    @Column(name = "FECHA", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;

    @JoinColumn(name = "ID_TIPO_MOVIMIENTO", referencedColumnName = "ID_TIPO_MOVIMIENTO", nullable = false)
    @ManyToOne(optional = false)
    private TipoMovimiento idTipoMovimiento;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "idComprobante", orphanRemoval = true)
    private Collection<ComprobanteDetalle> comprobanteDetalleCollection = new ArrayList<>();

    public ComprobanteCabecera() {}

    public BigDecimal getIdComprobante() { return idComprobante; }
    public void setIdComprobante(BigDecimal idComprobante) { this.idComprobante = idComprobante; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }
    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }
    public TipoMovimiento getIdTipoMovimiento() { return idTipoMovimiento; }
    public void setIdTipoMovimiento(TipoMovimiento idTipoMovimiento) { this.idTipoMovimiento = idTipoMovimiento; }

    @JsonbTransient
    public Collection<ComprobanteDetalle> getComprobanteDetalleCollection() { return comprobanteDetalleCollection; }
    public void setComprobanteDetalleCollection(Collection<ComprobanteDetalle> detalles) {
        this.comprobanteDetalleCollection = detalles == null ? new ArrayList<>() : detalles;
    }

    public Collection<ComprobanteDetalle> getDetalles() { return comprobanteDetalleCollection; }
    public void setDetalles(Collection<ComprobanteDetalle> detalles) {
        setComprobanteDetalleCollection(detalles);
    }
}
