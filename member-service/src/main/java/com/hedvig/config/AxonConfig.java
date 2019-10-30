package com.hedvig.config;

import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.sagas.MemberCreatedSaga;
import com.hedvig.memberservice.sagas.MemberSignedSaga;
import com.hedvig.memberservice.sagas.NameUpdateSaga;
import lombok.val;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AxonConfig {

  @Bean
  public AggregateFactory<MemberAggregate> memberAggregateFactory() {
    SpringPrototypeAggregateFactory<MemberAggregate> springPrototypeAggregateFactory =
        new SpringPrototypeAggregateFactory<>();
    springPrototypeAggregateFactory.setPrototypeBeanName("memberAggregate");

    return springPrototypeAggregateFactory;
  }


  @Bean("memberCreatedSagaSagaConfiguration")
  public SagaConfiguration<MemberCreatedSaga> memberCreatedSagaSagaConfiguration() {
    val config = SagaConfiguration.trackingSagaManager(MemberCreatedSaga.class);
    config.configureTrackingProcessor(
      x ->
        TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
          .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    return config;
  }


  @Bean("memberSignedSagaConfiguration")
  public SagaConfiguration<MemberSignedSaga> memberSignedSagaConfiguration() {

    val config = SagaConfiguration.trackingSagaManager(MemberSignedSaga.class);
    config.configureTrackingProcessor(
      x ->
        TrackingEventProcessorConfiguration.forParallelProcessing(2)
          .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    return config;
  }

  @Bean("memberNameUpdateSagaConfiguration")
  public SagaConfiguration<NameUpdateSaga> memberNameUpdateSagaConfiguration() {
    val config = SagaConfiguration.trackingSagaManager(NameUpdateSaga.class);
    config.configureTrackingProcessor(
      x ->
        TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
          .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    return config;
  }

  @Autowired
  public void configure(EventProcessingConfiguration config) {

    config.registerTrackingEventProcessor(
        "SegmentProcessorGroup",
        x ->
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    config.registerTrackingEventProcessor(
        "CustomerIO",
        x ->
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andInitialTrackingToken(StreamableMessageSource::createTailToken));
  }
}
