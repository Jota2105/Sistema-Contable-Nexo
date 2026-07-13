package com.grupo4.backend_api.integracion.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "INTEGRACION_HISTORIAL")
public class IntegracionHistorial implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_INTEGRACION")
    private Long idIntegracion;

    @Column(name = "ID_MENSAJE", nullable = false, unique = true, length = 255)
    private String idMensaje;

    @Column(name = "TIPO_EVENTO", nullable = false, length = 50)
    private String tipoEvento;

    @Column(name = "ORIGEN", nullable = false, length = 50)
    private String origen;

    @Column(name = "DESTINO", nullable = false, length = 50)
    private String destino;

    @Column(name = "ESTADO", nullable = false, length = 30)
    private String estado;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_PUBLICACION", nullable = false)
    private Date fechaPublicacion;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "FECHA_PROCESAMIENTO", nullable = false)
    private Date fechaProcesamiento;

    @Column(name = "INTENTOS", nullable = false)
    private Integer intentos;

    @Lob
    @Column(name = "PAYLOAD_JSON", nullable = false)
    private String payloadJson;

    @Column(name = "ID_COMPROBANTE")
    private java.math.BigDecimal idComprobante;

    @Column(name = "NUMERO_COMPROBANTE", length = 50)
    private String numeroComprobante;

    public Long getIdIntegracion() { return idIntegracion; }
    public void setIdIntegracion(Long idIntegracion) { this.idIntegracion = idIntegracion; }
    public String getIdMensaje() { return idMensaje; }
    public void setIdMensaje(String idMensaje) { this.idMensaje = idMensaje; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getOrigen() { return origen; }
    public void setOrigen(String origen) { this.origen = origen; }
    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Date getFechaPublicacion() { return fechaPublicacion; }
    public void setFechaPublicacion(Date fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public Date getFechaProcesamiento() { return fechaProcesamiento; }
    public void setFechaProcesamiento(Date fechaProcesamiento) { this.fechaProcesamiento = fechaProcesamiento; }
    public Integer getIntentos() { return intentos; }
    public void setIntentos(Integer intentos) { this.intentos = intentos; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public java.math.BigDecimal getIdComprobante() { return idComprobante; }
    public void setIdComprobante(java.math.BigDecimal idComprobante) { this.idComprobante = idComprobante; }
    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }
}
