package com.hedvig.external.billectaAPI;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.Properties;

@SpringBootApplication
public class TestApplication implements CommandLineRunner{
    public static void main(String[] args) {

        SpringApplication.run(TestApplication.class, args);
    }

    @Override
    public void run(String... strings) throws Exception {
        Properties props = System.getProperties();
        props.put("https.proxyHost", "127.0.0.1");
        props.put("https.proxyPort", "8080");


        BillectaApi api = new BillectaApi("myAwesomeCreditor", "muchSecureToken", new RestTemplate());
        api.BankIdAuth(Optional.empty());
    }

}