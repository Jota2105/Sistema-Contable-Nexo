package com.grupo4.backend_api.cobranzas.resource;

import com.grupo4.backend_api.cobranzas.modelo.PagoDetalle;
import com.grupo4.backend_api.cobranzas.negocio.NegocioPagoDetalle;
import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/pagos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class PagoResource {

    @Inject
    private NegocioPagoDetalle negocioPago;

    @GET
    public Response listar(@QueryParam("idFactura") Integer idFactura) {
        List<PagoDetalle> pagos = idFactura == null
                ? negocioPago.listarTodos()
                : negocioPago.listarPorFactura(idFactura);
        return Response.ok(new ApiResponse<>(200, "Pagos obtenidos exitosamente", pagos)).build();
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") Integer id) {
        PagoDetalle pago = negocioPago.buscar(id);
        if (pago == null) {
            throw new ApiException(Response.Status.NOT_FOUND, "Pago no encontrado con ID: " + id);
        }
        return Response.ok(new ApiResponse<>(200, "Pago obtenido exitosamente", pago)).build();
    }

    @POST
    public Response crear(PagoDetalle pago) {
        try {
            negocioPago.insertar(pago);
            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse<>(201, "Pago registrado exitosamente", pago))
                    .build();
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @PUT
    @Path("/{id}")
    public Response actualizar(@PathParam("id") Integer id, PagoDetalle pago) {
        pago.setIdPagoDetalle(id);
        try {
            if (negocioPago.modificar(pago) == 0) {
                throw new ApiException(Response.Status.NOT_FOUND, "Pago no encontrado con ID: " + id);
            }
            return Response.ok(new ApiResponse<>(200, "Pago actualizado exitosamente", negocioPago.buscar(id))).build();
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") Integer id) {
        if (negocioPago.eliminar(id) == 0) {
            throw new ApiException(Response.Status.NOT_FOUND, "Pago no encontrado con ID: " + id);
        }
        return Response.ok(new ApiResponse<Void>(200, "Pago eliminado exitosamente")).build();
    }
}
