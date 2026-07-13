package com.grupo4.backend_api.inventario.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.inventario.negocio.NegocioComprobante;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Path("/inventario/reportes")
@Produces(MediaType.APPLICATION_JSON)
@RequestScoped
public class InventarioReporteResource {

    @Inject
    private NegocioComprobante negocio;

    @GET
    @Path("/movimientos")
    public Response movimientos(
            @QueryParam("fechaInicio") String fechaInicio,
            @QueryParam("fechaFin") String fechaFin) {
        try {
            LocalDate inicio = LocalDate.parse(fechaInicio);
            LocalDate fin = LocalDate.parse(fechaFin);
            if (fin.isBefore(inicio)) {
                throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
            }

            Date desde = Date.from(inicio.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date hasta = Date.from(fin.plusDays(1).atStartOfDay(ZoneId.systemDefault()).minusNanos(1).toInstant());

            return Response.ok(new ApiResponse<>(
                    200,
                    "Reporte de movimientos generado exitosamente",
                    negocio.movimientos(desde, hasta))).build();
        } catch (RuntimeException e) {
            throw new ApiException(Response.Status.BAD_REQUEST,
                    "Las fechas deben enviarse en formato YYYY-MM-DD.");
        }
    }
}
