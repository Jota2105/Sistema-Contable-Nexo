package com.grupo4.backend_api.inventario.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.inventario.modelo.Articulo;
import com.grupo4.backend_api.inventario.negocio.NegocioArticulo;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("/articulos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class ArticuloResource {

    @Inject
    private NegocioArticulo negocioArticulo;

    @Inject
    private NegocioComprobante negocioComprobante;

    @GET
    public Response obtenerArticulos(@QueryParam("nombre") String nombre) {
        List<Articulo> lista = nombre != null && !nombre.trim().isEmpty()
                ? negocioArticulo.buscarPorNombre(nombre)
                : negocioArticulo.listarTodos();
        return Response.ok(new ApiResponse<>(200, "Artículos obtenidos exitosamente", lista)).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") BigDecimal idArticulo) {
        Articulo articulo = negocioArticulo.buscarPorId(idArticulo);
        if (articulo == null) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Artículo no encontrado con ID: " + idArticulo);
        }
        return Response.ok(new ApiResponse<>(200, "Artículo encontrado", articulo)).build();
    }

    @GET
    @Path("/{id}/stock")
    public Response obtenerStock(@PathParam("id") BigDecimal idArticulo) {
        Articulo articulo = negocioArticulo.buscarPorId(idArticulo);
        if (articulo == null) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Artículo no encontrado con ID: " + idArticulo);
        }
        int stock = negocioComprobante.stock(idArticulo);
        return Response.ok(new ApiResponse<>(200, "Stock obtenido exitosamente",
                Map.of("idArticulo", idArticulo, "stock", stock))).build();
    }

    @POST
    public Response crear(Articulo articulo) {
        int resultado = negocioArticulo.insertar(articulo);
        if (resultado != 1) {
            throw new ApiException(Response.Status.BAD_REQUEST, "No se pudo crear el artículo.");
        }
        return Response.status(Response.Status.CREATED)
                .entity(new ApiResponse<>(201, "Artículo creado exitosamente", articulo))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response modificar(@PathParam("id") BigDecimal idArticulo, Articulo articulo) {
        articulo.setIdArticulo(idArticulo);
        int resultado = negocioArticulo.modificar(articulo);
        if (resultado == 0) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Artículo no encontrado con ID: " + idArticulo);
        }
        if (resultado != 1) {
            throw new ApiException(Response.Status.BAD_REQUEST, "No se pudo actualizar el artículo.");
        }
        return Response.ok(new ApiResponse<>(200, "Artículo actualizado exitosamente", articulo)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminar(@PathParam("id") BigDecimal idArticulo) {
        int resultado = negocioArticulo.eliminar(idArticulo);
        if (resultado == 0) {
            throw new ApiException(Response.Status.NOT_FOUND,
                    "Artículo no encontrado con ID: " + idArticulo);
        }
        if (resultado != 1) {
            throw new ApiException(Response.Status.CONFLICT,
                    "El artículo no puede eliminarse porque tiene movimientos asociados.");
        }
        return Response.ok(new ApiResponse<Void>(200, "Artículo eliminado exitosamente")).build();
    }
}
