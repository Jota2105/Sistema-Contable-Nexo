package com.grupo4.backend_api.inventario.negocio;

import com.grupo4.backend_api.inventario.modelo.Articulo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class NegocioArticulo {

    private static final long LOCK_ARTICULO = 43001L;

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Transactional
    public int insertar(Articulo articulo) {
        try {
            validar(articulo);
            if (articulo.getIdArticulo() == null) {
                articulo.setIdArticulo(siguienteIdSeguro());
            }
            if (articulo.getFechaCreacion() == null) {
                articulo.setFechaCreacion(new Date());
            }
            em.persist(articulo);
            em.flush();
            return 1;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            return -1;
        }
    }

    @Transactional
    public int modificar(Articulo articulo) {
        try {
            validar(articulo);
            Articulo existente = em.find(Articulo.class, articulo.getIdArticulo());
            if (existente == null) {
                return 0;
            }
            existente.setNombre(articulo.getNombre().trim());
            existente.setPrecio(articulo.getPrecio());
            return 1;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            return -1;
        }
    }

    @Transactional
    public int eliminar(BigDecimal idArticulo) {
        try {
            Articulo articulo = em.find(Articulo.class, idArticulo);
            if (articulo == null) {
                return 0;
            }
            em.remove(articulo);
            em.flush();
            return 1;
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Articulo> buscarPorNombre(String valor) {
        return em.createQuery(
                "SELECT a FROM Articulo a WHERE LOWER(a.nombre) LIKE :valor ORDER BY a.nombre",
                Articulo.class)
                .setParameter("valor", "%" + valor.trim().toLowerCase() + "%")
                .getResultList();
    }

    public List<Articulo> listarTodos() {
        return em.createQuery("SELECT a FROM Articulo a ORDER BY a.nombre", Articulo.class)
                .getResultList();
    }

    public Articulo buscarPorId(int idArticulo) {
        return buscarPorId(BigDecimal.valueOf(idArticulo));
    }

    public Articulo buscarPorId(BigDecimal idArticulo) {
        return em.find(Articulo.class, idArticulo);
    }

    private void validar(Articulo articulo) {
        if (articulo == null) {
            throw new IllegalArgumentException("Los datos del artículo son obligatorios.");
        }
        if (articulo.getNombre() == null || articulo.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del artículo es obligatorio.");
        }
        if (articulo.getPrecio() == null || articulo.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        articulo.setNombre(articulo.getNombre().trim());
    }

    private BigDecimal siguienteIdSeguro() {
        em.createNativeQuery("SELECT pg_advisory_xact_lock(?1)")
                .setParameter(1, LOCK_ARTICULO)
                .getSingleResult();
        Number valor = (Number) em.createNativeQuery(
                "SELECT COALESCE(MAX(ID_ARTICULO), 0) + 1 FROM ARTICULO")
                .getSingleResult();
        return BigDecimal.valueOf(valor.longValue());
    }
}
