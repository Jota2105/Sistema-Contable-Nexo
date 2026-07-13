package com.grupo4.backend_api.integracion.dto;

import java.math.BigDecimal;
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
    private Map<String