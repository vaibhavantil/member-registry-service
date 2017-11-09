package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBException;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@SpringBootApplication
@EnableFeignClients
public class TestApplication implements CommandLineRunner{
    public static void main(String[] args) {

        SpringApplication.run(TestApplication.class, args);
    }

    @Autowired
    BillectaClient client;

    @Override
    public void run(String... strings) throws Exception {

        System.setProperty("https.proxyHost", "127.0.0.1");
        System.setProperty("https.proxyPort", "8080");


      //  client = Feign.builder().decoder(new JAXBDecoder( new JAXBContextFactory.Builder().build())).target(BillectaClient.class, "https://apitest.billecta.com");

        BillectaApi api = new BillectaApiImpl("ce24a223-7d39-49ac-b20a-34b61cf48ef3", "+fv7fzW9SG4uwvcnRwB2/anjsf+snZakt6RcPma8w4d6e5pjiyk4dxklLg1qZD/W87eE59RwmRbiC5bOZb1+WQ==", client);
        api.BankIdAuth(null);

        //Creditors creditors = api.getAllCreditors();
        //System.out.println(creditors.toString());

        ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(3);

        String bankAccountSuccessId = "198001139297";
        String bankAccountFailId = "197001239297";

        String a = api.initBankAccountRetreivals(bankAccountFailId, "FSPA");
        BankAccountPoller poller = new BankAccountPoller(a,
                api,
                executorService, (x) -> System.out.println(x.toString()),
                System.out::println);
        poller.run();
        //CreateDebtor(api);

    }


    private void CreateDebtor(BillectaApiImpl api) throws JAXBException {

        Debtor deb = new Debtor();
        deb.setDebtorExternalId("23445");
        deb.setCreditorPublicId("3ac96b99-58c7-4b41-a697-02a6f52818f2");
        deb.setName("Mr member number one");
        deb.setAddress("Långgatan 3");
        deb.setCity("Täby");
        deb.setZipCode("18774");
        deb.setDebtorType(DebtorType.PRIVATE);
        deb.setCountryCode("SE");
        deb.setEmail("member@email.com");
        deb.setContactEmail("kundtjant@hedvig.com");
        deb.setOrgNo("191212121212");

        DebtorAutogiro autogiro = new DebtorAutogiro();
        autogiro.setAccountNo("8881882");
        autogiro.setClearingNo("2021-3");
        autogiro.setPaymentServiceSupplier("FSPA");

        deb.setAutogiro(autogiro);

        api.createDebtor(deb);

    }

}