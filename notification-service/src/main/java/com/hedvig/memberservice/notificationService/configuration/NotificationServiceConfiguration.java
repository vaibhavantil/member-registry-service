package com.hedvig.memberservice.notificationService.configuration;

import com.hedvig.memberservice.notificationService.queue.MemberBCCAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationServiceConfiguration {

  @Bean
  List<MemberBCCAddress> bccAddresses(@Value("${hedvig.notificationService.memberBcc}") String[] bcc){
    return Arrays.stream(bcc).
        map(MemberBCCAddress::new)
        .collect(Collectors.toList());
  }

}
