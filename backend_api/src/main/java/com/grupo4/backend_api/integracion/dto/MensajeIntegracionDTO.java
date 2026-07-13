package com.grupo4.backend_api.integracion.dto;

import java.util.Map;

public class MensajeIntegracionDTO {

    private String idMensaje;
    private String tipoEvento;
    private String origen;
    private String destino;
    private String estado;
    private String fechaPublicacion;
    private String fechaProcesamiento;
    private Integer intentos;
    private Map<String, Object> payload;
    private Map<String, Object> comprobanteGenerado;

    public MensajeIntegracionDTO() {
    }

    public String getIdMensaje() {
        return idMensaje;
    }

    public void setIdMensaje(String idMensaje) {
        this.idMensaje = idMensaje;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getFechaProcesamiento() {
        return fechaProcesamiento;
    }

    public void setFechaProcesamiento(String fechaProcesamiento) {
        this.fechaProcesamiento = fechaProcesamiento;
    }

    public Integer getIntentos() {
        return intentos;
    }

    public void setIntentos(Integer intentos) {
        this.intentos = intentos;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public Map<String, Object> getComprobanteGenerado() {
        return comprobanteGenerado;
    }

    public void setComprobanteGenerado(Map<String, Object> comprobanteGenerado) {
        this.comprobanteGenerado = comprobanteGenerado;
    }
}
