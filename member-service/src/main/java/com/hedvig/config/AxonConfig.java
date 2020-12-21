package com.hedvig.config;

import com.hedvig.memberservice.aggregates.MemberAggregate;
import com.hedvig.memberservice.events.upcasters.OnboardingStartedWithSSNEventUpcaster;
import com.hedvig.memberservice.events.upcasters.SSNUpdatedEventUpcaster;
import com.hedvig.memberservice.sagas.MemberSignedSaga;
import lombok.val;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.serialization.upcasting.event.EventUpcasterChain;
import org.axonframework.spring.eventsourcing.SpringPrototypeAggregateFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

  @Bean
  public AggregateFactory<MemberAggregate> memberAggregateFactory() {
    SpringPrototypeAggregateFactory<MemberAggregate> springPrototypeAggregateFactory =
        new SpringPrototypeAggregateFactory<>();
    springPrototypeAggregateFactory.setPrototypeBeanName("memberAggregate");

    return springPrototypeAggregateFactory;
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

  @Autowired
  public void configure(EventProcessingConfiguration config) {

    config.registerTrackingEventProcessor(
        "SegmentProcessorGroup",
        x ->
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    config.registerTrackingEventProcessor(
        "CleanCustomerIO",
        x ->
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andInitialTrackingToken(StreamableMessageSource::createTailToken));

    config.registerTrackingEventProcessor(
        "CreateCampaignOwner",
        x ->
            TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                .andInitialTrackingToken(StreamableMessageSource::createHeadToken));

      config.registerTrackingEventProcessor(
          "NotificationServiceUpdatePhoneNumber",
          x ->
              TrackingEventProcessorConfiguration.forSingleThreadedProcessing()
                  .andInitialTrackingToken(StreamableMessageSource::createTailToken));
  }


    @Bean
    public EventUpcasterChain eventUpcasters() {
        return new EventUpcasterChain(
            new OnboardingStartedWithSSNEventUpcaster(),
            new SSNUpdatedEventUpcaster()
        );
    }
}
