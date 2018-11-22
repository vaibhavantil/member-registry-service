package com.hedvig.memberservice.services.segmentpublisher;

import com.google.common.collect.ImmutableMap;
import com.hedvig.memberservice.events.EmailUpdatedEvent;
import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.segment.analytics.Analytics;
import com.segment.analytics.messages.IdentifyMessage;
import java.util.Map;
import java.util.Objects;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("SegmentProcessorGroup")
public class EventListener {

  private final Analytics segmentAnalytics;

  public EventListener(Analytics analytics) {
    this.segmentAnalytics = analytics;
  }

  @EventHandler
  public void on(NameUpdatedEvent evt) {
    final ImmutableMap<String, Object> traits = ImmutableMap
        .of("first_name", evt.getFirstName(), "last_name", evt.getLastName());
    segmentEnqueue(traits, Objects.toString(evt.getMemberId()));
  }

  @EventHandler
  public void on(EmailUpdatedEvent evt) {
    final ImmutableMap<String, Object> traits = ImmutableMap
        .of("email", evt.getEmail(),
            "timezone", "Europe/Stockholm ");
    segmentEnqueue(traits, Objects.toString(evt.getId()));
  }

  private void segmentEnqueue(Map<String, Object> traitsMap, String memberId) {
    segmentAnalytics.enqueue(
        IdentifyMessage.builder()
            .userId(memberId)
            .enableIntegration("All", false)
            .enableIntegration("Customer.io", true)
            .traits(traitsMap));
  }
}
