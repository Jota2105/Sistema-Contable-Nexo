package com.grupo4.backend_api.facturacion.resource;

import com.grupo4.backend_api.core.ApiException;
import com.grupo4.backend_api.core.ApiResponse;
import com.grupo4.backend_api.facturacion.modelo.CiudadEntrega;
import com.grupo4.backend_api.facturacion.negocio.NegocioCiudad;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;

@Path("/ciudades")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CiudadResource {

    @Inject
    private NegocioCiudad negocioCiudad;

    @GET
    public Response obtenerCiudades(@QueryParam("nombre") String nombre) {
        List<CiudadEntrega> lista = (nombre == null || nombre.isBlank())
                ? negocioCiudad.listarTodos()
                : negocioCiudad.buscarPorNombre(nombre);

        if (lista == null) {
            throw new ApiException(Status.INTERNAL_SERVER_ERROR, "No fue posible recuperar las ciudades.");
        }

        return Response.ok(new ApiResponse<>(200, "Ciudades obtenidas exitosamente", lista)).build();
    }

    @GET
    @Path("/{id}")
    public Response buscarPorId(@PathParam("id") Integer idCiudad) {
        CiudadEntrega ciudad = negocioCiudad.buscar(idCiudad);
        if (ciudad == null) {
            throw new ApiException(Status.NOT_FOUND, "Ciudad no encontrada con ID: " + idCiudad);
        }
        return Response.ok(new ApiResponse<>(200, "Ciudad obtenida exitosamente", ciudad)).build();
    }

    @POST
    public Response crearCiudad(CiudadEntrega ciudad) {
        if (ciudad == null || ciudad.getNombre() == null || ciudad.getNombre().isBlank()) {
            throw new ApiException(Status.BAD_REQUEST, "El nombre de la ciudad es obligatorio.");
        }
        int resultado = negocioCiudad.insertar(ciudad);
        if (resultado == -1) {
            throw new ApiException(Status.INTERNAL_SERVER_ERROR, "Error interno al crear la ciudad.");
        }
        return Response.status(Status.CREATED)
                .entity(new ApiResponse<>(201, "Ciudad creada exitosamente", ciudad))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response actualizarCiudad(@PathParam("id") Integer idCiudad, CiudadEntrega ciudad) {
        if (ciudad == null || ciudad.getNombre() == null || ciudad.getNombre().isBlank()) {
            throw new ApiException(Status.BAD_REQUEST, "El nombre de la ciudad es obligatorio.");
        }
        ciudad.setIdCiudad(idCiudad);
        int resultado = negocioCiudad.modificar(ciudad);
        if (resultado == 0) {
            throw new ApiException(Status.NOT_FOUND, "Ciudad no encontrada con ID: " + idCiudad);
        }
        if (resultado == -1) {
            throw new ApiException(Status.INTERNAL_SERVER_ERROR, "Error interno al actualizar la ciudad.");
        }
        return Response.ok(new ApiResponse<>(200, "Ciudad actualizada exitosamente", ciudad)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response eliminarCiudad(@PathParam("id") Integer id) {
        int resultado = negocioCiudad.eliminar(id);
        if (resultado == 0) {
            throw new ApiException(Status.NOT_FOUND, "Ciudad no encontrada con ID: " + id);
        }
        if (resultado == -1) {
            throw new ApiException(Status.INTERNAL_SERVER_ERROR, "Error interno al eliminar la ciudad.");
        }
        return Response.ok(new ApiResponse<Void>(200, "Ciudad eliminada exitosamente")).build();
    }
}
