package com.hedvig.memberservice.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["com.hedvig.external.syna"])
class SynaConfiguration { }
