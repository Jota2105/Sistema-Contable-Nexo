package com.grupo4.backend_api.config;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.enterprise.context.ApplicationScoped;

@DataSourceDefinition(
    name = "java:global/jdbc/NexoDS",
    className = "org.postgresql.ds.PGSimpleDataSource",
    user = "postgres",
    password = "postgres",
    databaseName = "nexo_db",
    serverName = "haproxy",
    portNumber = 5432,
    minPoolSize = 2,
    maxPoolSize = 20
)
@ApplicationScoped
public class DatabaseConfig {
}
