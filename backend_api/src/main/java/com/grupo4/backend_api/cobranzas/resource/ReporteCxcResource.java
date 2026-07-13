package com.grupo4.backend_api.cobranzas.resource;

import com.grupo4.backend_api.cobranzas.negocio.NegocioReportes;
import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Path("/cxc/reportes")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class ReporteCxcResource {

    @Inject
    private NegocioReportes negocioReportes;

    @GET
    @Path("/estado-cuenta")
    public Response estadoCuenta(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = convertirFecha(fechaInicio, "fechaInicio");
            Date fin = convertirFecha(fechaFin, "fechaFin");
            return Response.ok(new ApiResponse<>(
                    200,
                    "Estado de cuenta generado exitosamente",
                    negocioReportes.generarEstadoCuentaPorFechas(inicio, fin))).build();
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @GET
    @Path("/matriz-recaudacion")
    public Response matrizRecaudacion(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            Date inicio = convertirFecha(fechaInicio, "fechaInicio");
            Date fin = convertirFecha(fechaFin, "fechaFin");
            return Response.ok(new ApiResponse<>(
                    200,
                    "Matriz de recaudación generada exitosamente",
                    negocioReportes.generarMatrizCruzada(inicio, fin))).build();
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    private Date convertirFecha(String valor, String nombreParametro) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("El parámetro " + nombreParametro + " es obligatorio.");
        }
        try {
            return Date.valueOf(LocalDate.parse(valor));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "El parámetro " + nombreParametro + " debe tener formato YYYY-MM-DD.");
        }
    }
}
