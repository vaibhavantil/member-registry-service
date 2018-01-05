package com.hedvig.memberservice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.hedvig.external.bankID"})
public class ExternalConfiguration {
}
