package com.grupo4.backend_api.inventario.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.inventario.modelo.ComprobanteCabecera;
import com.grupo4.backend_api.inventario.negocio.NegocioComprobante;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;

@Path("/comprobantes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ComprobanteResource {

    @Inject
    private NegocioComprobante negocio;

    @GET
    public Response listar() {
        return Response.ok(new ApiResponse<>(
                200,
                "Comprobantes obtenidos exitosamente",
                negocio.listar())).build();
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") BigDecimal id) {
        ComprobanteCabecera comprobante = negocio.buscar(id);
        if (comprobante == null) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Comprobante no encontrado con ID: " + id);
        }
        return Response.ok(new ApiResponse<>(200, "Comprobante obtenido exitosamente", comprobante)).build();
    }

    @POST
    public Response crear(ComprobanteCabecera comprobante) {
        try {
            ComprobanteCabecera creado = negocio.crear(comprobante);
            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse<>(201, "Comprobante creado exitosamente", creado))
                    .build();
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") BigDecimal id, ComprobanteCabecera comprobante) {
        try {
            ComprobanteCabecera actualizado = negocio.actualizar(id, comprobante);
            if (actualizado == null) {
                throw new ApiException(Response.Status.NOT_FOUND,
                        "Comprobante no encontrado con ID: " + id);
            }
            return Response.ok(new ApiResponse<>(200, "Comprobante actualizado exitosamente", actualizado)).build();
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") BigDecimal id) {
        if (!negocio.eliminar(id)) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Comprobante no encontrado con ID: " + id);
        }
        return Response.ok(new ApiResponse<Void>(200, "Comprobante eliminado exitosamente")).build();
    }
}
