package com.hedvig.memberservice.config;

import com.segment.analytics.Analytics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SegmentConfiguration {

  @Bean
  public Analytics segmentAnalytics(@Value("${hedvig.segment.writeKey}")String writeKey) {
    return Analytics.builder(writeKey).build();
  }


}
