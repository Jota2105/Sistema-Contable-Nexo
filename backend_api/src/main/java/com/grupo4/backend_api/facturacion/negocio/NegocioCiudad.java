package com.grupo4.backend_api.facturacion.negocio;

import com.grupo4.backend_api.facturacion.modelo.CiudadEntrega;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

@ApplicationScoped
public class NegocioCiudad {

    @PersistenceContext(unitName = "SistemaContablePU")
    private EntityManager em;

    @Transactional
    public int insertar(CiudadEntrega ciudad) {
        try {
            if (ciudad.getIdCiudad() == null) {
                ciudad.setIdCiudad(obtenerSiguienteId());
            }
            em.persist(ciudad);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Transactional
    public int modificar(CiudadEntrega ciudad) {
        try {
            CiudadEntrega existente = em.find(CiudadEntrega.class, ciudad.getIdCiudad());
            if (existente == null) {
                return 0;
            }
            existente.setNombre(ciudad.getNombre());
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Transactional
    public int eliminar(Integer idCiudad) {
        try {
            CiudadEntrega ciudad = em.find(CiudadEntrega.class, idCiudad);
            if (ciudad == null) {
                return 0;
            }
            em.remove(ciudad);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public CiudadEntrega buscar(Integer idCiudad) {
        return em.find(CiudadEntrega.class, idCiudad);
    }

    public List<CiudadEntrega> buscarPorNombre(String valor) {
        return em.createQuery(
                "SELECT c FROM CiudadEntrega c WHERE LOWER(c.nombre) LIKE :valor ORDER BY c.nombre",
                CiudadEntrega.class)
                .setParameter("valor", "%" + valor.trim().toLowerCase() + "%")
                .getResultList();
    }

    public List<CiudadEntrega> listarTodos() {
        return em.createQuery(
                "SELECT c FROM CiudadEntrega c ORDER BY c.nombre",
                CiudadEntrega.class)
                .getResultList();
    }

    public Integer obtenerSiguienteId() {
        Number maximo = (Number) em.createQuery(
                "SELECT COALESCE(MAX(c.idCiudad), 0) FROM CiudadEntrega c")
                .getSingleResult();
        return maximo.intValue() + 1;
    }
}
