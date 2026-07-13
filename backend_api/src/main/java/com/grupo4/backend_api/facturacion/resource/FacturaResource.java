package com.grupo4.backend_api.facturacion.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.facturacion.modelo.FacturaCabecera;
import com.grupo4.backend_api.facturacion.modelo.FacturaDetalle;
import com.grupo4.backend_api.facturacion.negocio.NegocioFactura;
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
import java.util.List;

@Path("/facturas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class FacturaResource {

    @Inject
    private NegocioFactura negocioFactura;

    @GET
    public Response listar() {
        List<FacturaCabecera> facturas = negocioFactura.listarTodos();
        return Response.ok(new ApiResponse<>(200, "Facturas obtenidas exitosamente", facturas)).build();
    }

    @GET
    @Path("/{id}")
    public Response buscar(@PathParam("id") Integer id) {
        FacturaCabecera factura = negocioFactura.buscar(id);
        if (factura == null) {
            throw new ApiException(Response.Status.NOT_FOUND, "Factura no encontrada con ID: " + id);
        }
        return Response.ok(new ApiResponse<>(200, "Factura obtenida exitosamente", factura)).build();
    }

    @POST
    public Response crear(FacturaCabecera factura) {
        try {
            negocioFactura.insertar(factura);
            return Response.status(Response.Status.CREATED)
                    .entity(new ApiResponse<>(201, "Factura creada exitosamente", factura))
                    .build();
        } catch (IllegalStateException e) {
            throw new ApiException(Response.Status.CONFLICT, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") Integer id) {
        if (negocioFactura.eliminar(id) == 0) {
            throw new ApiException(Response.Status.NOT_FOUND, "Factura no encontrada con ID: " + id);
        }
        return Response.ok(new ApiResponse<Void>(200, "Factura eliminada exitosamente")).build();
    }

    @PUT
    @Path("/detalle/{idDetalle}")
    public Response modificarDetalle(
            @PathParam("idDetalle") Integer idDetalle,
            FacturaDetalle detalle) {
        detalle.setIdFacturaDet(idDetalle);
        try {
            if (negocioFactura.modificarDetalle(detalle) == 0) {
                throw new ApiException(Response.Status.NOT_FOUND,
                        "Detalle de factura no encontrado con ID: " + idDetalle);
            }
            return Response.ok(new ApiResponse<>(200, "Detalle actualizado exitosamente", detalle)).build();
        } catch (IllegalArgumentException e) {
            throw new ApiException(Response.Status.BAD_REQUEST, e.getMessage());
        }
    }

    @DELETE
    @Path("/detalle/{idDetalle}")
    public Response eliminarDetalle(@PathParam("idDetalle") Integer idDetalle) {
        if (negocioFactura.eliminarDetalle(idDetalle) == 0) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Detalle de factura no encontrado con ID: " + idDetalle);
        }
        return Response.ok(new ApiResponse<Void>(200, "Detalle eliminado exitosamente")).build();
    }

    @GET
    @Path("/reportes/ventas-por-ciudad")
    public Response ventasPorCiudad() {
        return Response.ok(new ApiResponse<>(
                200,
                "Reporte de ventas por ciudad generado exitosamente",
                negocioFactura.reporteVentasPorCiudad())).build();
    }

    @GET
    @Path("/reportes/matriz-ventas")
    public Response matrizVentas() {
        return Response.ok(new ApiResponse<>(
                200,
                "Matriz de ventas generada exitosamente",
                negocioFactura.reporteMatrizVentas())).build();
    }
}
