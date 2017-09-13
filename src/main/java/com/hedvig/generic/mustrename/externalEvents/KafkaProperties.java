package com.hedvig.generic.mustrename.externalEvents;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "hedvig")
public class KafkaProperties {
    public String bootstrapServers;

    public String acks;

    public int retries;

    public String keySerializer;

    public String valueSerializer;

    public String schemaRegistryUrl;
}
