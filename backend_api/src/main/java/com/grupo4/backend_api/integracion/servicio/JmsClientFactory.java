package com.grupo4.backend_api.integracion.servicio;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.apache.activemq.artemis.jms.client.ActiveMQQueue;

@ApplicationScoped
public class JmsClientFactory {

    private static final String DEFAULT_BROKER_URL = "tcp://artemis:61616";
    private static final String DEFAULT_USER = "nexo";
    private static final String DEFAULT_PASSWORD = "nexo123";
    private static final String DEFAULT_QUEUE = "facturaCreadaQueue";

    public JMSContext crearContexto(int sessionMode) {
        String brokerUrl = valorEntorno("JMS_BROKER_URL", DEFAULT_BROKER_URL);
        String usuario = valorEntorno("JMS_USER", DEFAULT_USER);
        String clave = valorEntorno("JMS_PASSWORD", DEFAULT_PASSWORD);

        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        return connectionFactory.createContext(usuario, clave, sessionMode);
    }

    public Queue facturaCreadaQueue() {
        return new ActiveMQQueue(valorEntorno("JMS_FACTURA_QUEUE", DEFAULT_QUEUE));
    }

    private String valorEntorno(String nombre, String defecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? defecto : valor.trim();
    }
}
