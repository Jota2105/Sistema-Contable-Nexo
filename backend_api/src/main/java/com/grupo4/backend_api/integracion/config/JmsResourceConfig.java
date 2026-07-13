package com.grupo4.backend_api.integracion.config;

import jakarta.jms.JMSDestinationDefinition;

@JMSDestinationDefinition(
        name = "java:global/jms/facturaCreadaQueue",
        interfaceName = "jakarta.jms.Queue",
        destinationName = "facturaCreadaQueue"
)
public class JmsResourceConfig {
}
