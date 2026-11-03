package com.juanvargas.kubesynapse.infrastructure.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ======================================================================================
 * CONFIGURACIÓN DE INFRAESTRUCTURA: RABBITMQ (AMQP Broker)
 * ======================================================================================
 * Rol en la arquitectura:
 * - Declara los elementos de mensajería para Arquitectura Orientada a Eventos (EDA):
 *   1. Exchange de tipo Topic: Permite enrutamiento flexible por patrón (ej. k8s.incident.*).
 *   2. Cola de Incidentes: Recibe los eventos para consumo asíncrono.
 *   3. Binding: Vincula el exchange con la cola mediante la clave de ruteo.
 *   4. Jackson2JsonMessageConverter: Serializa los eventos a formato JSON estándar.
 *
 * Referencias educativas:
 * - RabbitMQ AMQP Concepts: https://www.rabbitmq.com/tutorials/tutorial-five-spring-amqp.html
 * - Spring AMQP Reference: https://docs.spring.io/spring-amqp/reference/
 * ======================================================================================
 */
@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE_NAME = "k8s.incidents.exchange";
    public static final String QUEUE_NAME = "k8s.incidents.queue";
    public static final String ROUTING_KEY = "k8s.incident.*";

    @Bean
    public TopicExchange incidentExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue incidentQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding incidentBinding(Queue incidentQueue, TopicExchange incidentExchange) {
        return BindingBuilder.bind(incidentQueue).to(incidentExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
