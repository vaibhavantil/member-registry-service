package com.hedvig.generic.mustrename;

import org.axonframework.config.EventHandlingConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MustRenameApplication {

	public static void main(String[] args) {
		SpringApplication.run(MustRenameApplication.class, args);
	}

    @Autowired
    public void configure(EventHandlingConfiguration config) {
        config.usingTrackingProcessors();
    }
}
