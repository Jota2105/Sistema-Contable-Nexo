package com.grupo4.backend_api.inventario.modelo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "COMPROBANTE_DETALLE")
public class ComprobanteDetalle implements Serializable {

    @Id
    @Basic(optional = false)
    @Column(name = "ID_COMPROBANTE_DET")
    private BigDecimal idComprobanteDet;

    @Basic(optional = false)
    @Column(name = "CANTIDAD", nullable = false)
    private BigInteger cantidad;

    @Basic(optional = false)
    @Column(name = "PRECIO", nullable = false)
    private BigDecimal precio;

    @JoinColumn(name = "ID_ARTICULO", referencedColumnName = "ID_ARTICULO", nullable = false)
    @ManyToOne(optional = false)
    private Articulo idArticulo;

    @JoinColumn(name = "ID_COMPROBANTE", referencedColumnName = "ID_COMPROBANTE", nullable = false)
    @ManyToOne(optional = false)
    private ComprobanteCabecera idComprobante;

    public ComprobanteDetalle() {}

    public BigDecimal getIdComprobanteDet() { return idComprobanteDet; }
    public void setIdComprobanteDet(BigDecimal idComprobanteDet) { this.idComprobanteDet = idComprobanteDet; }
    public BigInteger getCantidad() { return cantidad; }
    public void setCantidad(BigInteger cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Articulo getIdArticulo() { return idArticulo; }
    public void setIdArticulo(Articulo idArticulo) { this.idArticulo = idArticulo; }

    @JsonbTransient
    public ComprobanteCabecera getIdComprobante() { return idComprobante; }
    public void setIdComprobante(ComprobanteCabecera idComprobante) { this.idComprobante = idComprobante; }
}
