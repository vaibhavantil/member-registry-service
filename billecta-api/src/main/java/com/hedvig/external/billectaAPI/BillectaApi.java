package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BillectaApi {

    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(BillectaApi.class);

    private String baseUrl; //"https://api.billecta.com/v1/"

    private final String creditorId;
    private final String secureToken;
    private final BillectaClient billectaClient;
    private ScheduledExecutorService executorService;

    public BillectaApi(
            String creditorId,
            String secureToken,
            RestTemplate restTemplate,
            String baseUrl,
            BillectaClient client,
            ScheduledExecutorService executorService) {
        this.creditorId = creditorId;
        this.secureToken = secureToken;
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.billectaClient = client;
        this.executorService = executorService;
    }

    public BillectaApi(
            String creditorId,
            String secureToken,
            BillectaClient client) {
        this.creditorId = creditorId;
        this.secureToken = secureToken;
        this.restTemplate = null;
        this.baseUrl = null;
        billectaClient = client;
    }

    public BankIdAuthenticationStatus BankIdAuth(String ssn) {
        HttpEntity<String> entity = createHeaders();

        String ssnOrEmpty = ssn == null ? "": ssn;


        UriTemplate uri = new UriTemplate(baseUrl + "/v1/bankid/authentication/{creditorId}?ssn={ssn}");
        URI expandedURI = uri.expand(new HashMap<String, Object>() {{
            put("creditorId", creditorId);
            put("ssn", ssnOrEmpty);
        }});
        System.out.println(expandedURI.toString());

        ResponseEntity<BankIdAuthenticationStatus> status = restTemplate.exchange(
                expandedURI,
                HttpMethod.PUT,
                entity,
                BankIdAuthenticationStatus.class
                );

        return status.getBody();
    }

    private HttpEntity<String> createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", EncodeTokenHeader());
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        return new HttpEntity<>("", headers);
    }

    private String EncodeTokenHeader() {
        return "SecureToken " + Base64.getEncoder().encodeToString(secureToken.getBytes());
    }


    public BankIdAuthenticationStatus BankIdCollect(String token) {
        HttpEntity<String> entity = createHeaders();

        ResponseEntity<BankIdAuthenticationStatus> status = restTemplate.exchange(
                baseUrl + "/v1/bankid/authentication/{token}",
                HttpMethod.GET,
                entity,
                BankIdAuthenticationStatus.class,
                token);

        return status.getBody();
    }

    public Creditors getAllCreditors(){
        assert this.billectaClient != null;
        return this.billectaClient.getAllCreditors(EncodeTokenHeader());
    }

    public void createDebtor(Debtor debtor) throws JAXBException {
        assert this.billectaClient != null;

        this.billectaClient.createDebtor(EncodeTokenHeader(), marshallToXML(debtor));
    }

    public String initBankAccountRetreivals(String ssn, String bankId) {
        logger.info("Init account Retreivals [{},{},{}]", creditorId,bankId,ssn);
        assert this.billectaClient != null;
        Created c = this.billectaClient.initiateBankAccountRetrieval(
                new LinkedMultiValueMap<>(),
                EncodeTokenHeader(),
                creditorId,
                bankId,
                ssn);
        return c.getPublicId();
    }

    public ResponseEntity<BankAccountRequest> getBankAccountNumbers(String publicId) {
        assert this.billectaClient != null;

        return this.billectaClient.getBankAccountNumbers(EncodeTokenHeader(), publicId);
    }

    public void retrieveBankAccountNumbers(
            String ssn,
            String bankId,
            Consumer<BankAccountRequest> onComplete,
            Consumer<String> onError) {
        String publicId = this.initBankAccountRetreivals(ssn, bankId);
        BankAccountPoller poller = new BankAccountPoller(publicId, this, executorService, onComplete, onError);
        executorService.schedule(poller,3, TimeUnit.SECONDS);
    }

    private String marshallToXML(Debtor bankIdAuthenticationStatus) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Debtor.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter strWr = new StringWriter();
        ObjectFactory factory = new ObjectFactory();
        marshaller.marshal(factory.createDebtor(bankIdAuthenticationStatus), strWr);
        return strWr.toString();
    }

}
