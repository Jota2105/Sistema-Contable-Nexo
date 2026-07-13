package com.grupo4.backend_api.system.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.InetAddress;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/system")
@Produces(MediaType.APPLICATION_JSON)
public class SystemResource {

    @GET
    @Path("/instance")
    public Map<String, Object> instance() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("instance", env("INSTANCE_NAME", "payara-local"));
        response.put("host", hostname());
        response.put("port", env("APP_DISPLAY_PORT", "8080"));
        return response;
    }

    @GET
    @Path("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "instance", env("INSTANCE_NAME", "payara-local"));
    }

    private String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}
