package com.hedvig.generic.mustrename.externalEvents;

import com.hedvig.generic.event.UserCreatedEvent_v1;
import com.hedvig.generic.event.UserEvent_v1;
import com.hedvig.generic.mustrename.events.UserCreatedEvent;
import org.apache.jute.Record;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class KafkaPublisher {

    KafkaProducer<String, UserEvent_v1> producer;

    @Autowired
    public KafkaPublisher(KafkaProperties properties) {
        System.out.println("Configuring KafkaPublisher");

        Properties props = new Properties();

        props.put("bootstrap.servers", properties.bootstrap.servers);
        props.put("acks", properties.acks);
        props.put("retries", properties.retries);
        props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
        props.put("schema.registry.url", properties.schema.registry.url);



//        UserCreatedEvent_v1 payload = new UserCreatedEvent_v1();
//        payload.setId(UUID.randomUUID().toString());
//        payload.setName("Johan");
//
//        e.setEventPayload(payload);

        this.producer = new KafkaProducer<>(props);

//        ProducerRecord<String, UserEvent_v1> pr = new ProducerRecord<>("testTopic", "nyckel", e);
//        Future<RecordMetadata> future = producer.send(pr);
//
//        try {
//            RecordMetadata metadata = future.get();
//            System.out.println(metadata);
//        } catch (InterruptedException e1) {
//            e1.printStackTrace();
//        } catch (ExecutionException e1) {
//            e1.printStackTrace();
//        }

    }

    @EventHandler
    public void on(UserCreatedEvent internalEvent, EventMessage message) throws ExecutionException, InterruptedException {
        UserEvent_v1 e = new UserEvent_v1();
        e.setCreatedAt(message.getTimestamp().toString());
        e.setEventId(message.getIdentifier());

        UserCreatedEvent_v1 externalEvent = new UserCreatedEvent_v1();
        externalEvent.setId(internalEvent.getId());
        externalEvent.setName(internalEvent.getName());
        e.setEventPayload(externalEvent);

        ProducerRecord<String, UserEvent_v1> pr = new ProducerRecord<>("UserEvents", internalEvent.getId(), e);
        Future<RecordMetadata> future = this.producer.send(pr);
        this.producer.flush();
        RecordMetadata recordMetadata = future.get();
    }
}
