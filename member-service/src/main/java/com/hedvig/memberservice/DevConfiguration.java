package com.hedvig.memberservice;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("development")
@ComponentScan(lazyInit = true)
public class DevConfiguration {

}
