package com.grupo4.backend_api.integracion.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.integracion.dto.MensajeIntegracionDTO;
import com.grupo4.backend_api.integracion.servicio.IntegracionService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/integracion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class IntegracionResource {

    @Inject
    private IntegracionService integracionService;

    @GET
    @Path("/cola")
    public Response listarCola() {
        List<MensajeIntegracionDTO> mensajes = integracionService.listarCola();
        return Response.ok(new ApiResponse<>(200, "Mensajes pendientes obtenidos exitosamente", mensajes)).build();
    }

    @GET
    @Path("/historial")
    public Response listarHistorial() {
        List<MensajeIntegracionDTO> historial = integracionService.listarHistorial();
        return Response.ok(new ApiResponse<>(200, "Historial de integración obtenido exitosamente", historial)).build();
    }

    @POST
    @Path("/cola/{idMensaje}/importar")
    public Response importar(@PathParam("idMensaje") String idMensaje) {
        try {
            MensajeIntegracionDTO procesado = integracionService.importar(idMensaje);
            return Response.ok(new ApiResponse<>(200, "Factura importada exitosamente en inventario", procesado)).build();
        } catch (NoResultException e) {
            throw new ApiException(Response.Status.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        }
    }
}
