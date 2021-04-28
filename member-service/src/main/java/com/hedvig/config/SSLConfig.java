package com.hedvig.config;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SSLConfig {
  @Autowired
  private Environment env;

  @PostConstruct
  private void configureSSL() {
    //set to TLSv1.1 or TLSv1.2
    System.setProperty("https.protocols", "TLSv1.2");

    //load the 'javax.net.ssl.trustStore' and
    //'javax.net.ssl.trustStorePassword' from application.properties
      String keyStore = env.getProperty("http.client.ssl.key-store");
      if (keyStore != null) {
          System.setProperty("javax.net.ssl.keyStore", keyStore);
      }
      String password = env.getProperty("http.client.ssl.key-store-password");
      if (password != null) {
          System.setProperty("javax.net.ssl.keyStorePassword", password);
      }
  }
}
