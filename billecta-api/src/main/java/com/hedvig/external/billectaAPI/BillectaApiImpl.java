package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;

import java.io.StringWriter;
import java.net.URI;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BillectaApiImpl implements BillectaApi {

    private RestTemplate restTemplate;

    private Logger logger = LoggerFactory.getLogger(BillectaApiImpl.class);

    private String baseUrl; //"https://api.billecta.com/v1/"

    private final String creditorId;
    private final String secureToken;
    private final BillectaClient billectaClient;
    private ScheduledExecutorService executorService;

    public BillectaApiImpl(
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

    public BillectaApiImpl(
            String creditorId,
            String secureToken,
            BillectaClient client) {
        this.creditorId = creditorId;
        this.secureToken = secureToken;
        this.restTemplate = null;
        this.baseUrl = null;
        billectaClient = client;
    }

    @Override
    public BankIdAuthenticationStatus BankIdAuth(String ssn) {
    	logger.info("BankIdAuth for ssn: " + ssn);
        HttpEntity<String> entity = createHeaders();

        String ssnOrEmpty = ssn == null ? "": ssn;

        UriTemplate uri = new UriTemplate(baseUrl + "/v1/bankid/authentication/{creditorId}?ssn={ssn}");
        URI expandedURI = uri.expand(new HashMap<String, Object>() {{
            put("creditorId", creditorId);
            put("ssn", ssnOrEmpty);
        }});
        logger.debug("expandedURI:" + expandedURI.toString());
        //System.out.println(expandedURI.toString());

        ResponseEntity<BankIdAuthenticationStatus> status = restTemplate.exchange(
                expandedURI,
                HttpMethod.PUT,
                entity,
                BankIdAuthenticationStatus.class
                );

        logger.debug("status.getBody():" + status.getBody().toString());
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
    	assert this.secureToken != null;
        return "SecureToken " + Base64.getEncoder().encodeToString(secureToken.getBytes());
    }

    @Override
    public BankIdAuthenticationStatus BankIdCollect(String token) {
    	logger.info("BankIdCollect for token:" + token);
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
        assert debtor != null;
        this.billectaClient.createDebtor(EncodeTokenHeader(), marshallToXML(debtor));
    }

    @Override
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

    @Override
    public ResponseEntity<BankAccountRequest> getBankAccountNumbers(String publicId) {
        assert this.billectaClient != null;
        assert publicId != null;
        return this.billectaClient.getBankAccountNumbers(EncodeTokenHeader(), publicId);
    }

    @Override
    public String retrieveBankAccountNumbers(
            String ssn,
            String bankId,
            Consumer<BankAccountRequest> onComplete,
            Consumer<String> onError) {
    	
    	logger.info("retrieveBankAccountNumbers: (ssn:" + ssn +")(bankId:" + bankId + ")");
        String publicId = this.initBankAccountRetreivals(ssn, bankId);
        BankAccountPoller poller = new BankAccountPoller(publicId, this, executorService, onComplete, onError);
        executorService.schedule(poller,3, TimeUnit.SECONDS);

        return publicId;
    }

    private String marshallToXML(Debtor bankIdAuthenticationStatus) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Debtor.class);
       	assert jaxbContext != null;
       	
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter strWr = new StringWriter();
        ObjectFactory factory = new ObjectFactory();
        marshaller.marshal(factory.createDebtor(bankIdAuthenticationStatus), strWr);
        assert strWr != null;
        
        return strWr.toString();
    }

    @Override
    public BankIdSignStatus BankIdSign(String ssn, String usermessage) {
    	
    	logger.info("BankIdSign: (ssn:" + ssn +")(usermessage:" + usermessage + ")");
    	assert this.billectaClient != null;
        ResponseEntity<BankIdSignStatus> status = this.billectaClient.bankIdSign(new LinkedMultiValueMap<>(), EncodeTokenHeader(), creditorId, ssn, usermessage);
        if(status.getStatusCode().is2xxSuccessful()) {
            return status.getBody();
        }

        throw new RuntimeException("Could not sign document: " + status.getStatusCode().toString());
    }

    @Override
    public BankIdSignStatus bankIdSignCollect(String referenceToken) {
    	logger.info("bankIdSignCollect: (referenceToken:" + referenceToken +")");
    	assert this.billectaClient != null;
    	
        ResponseEntity<BankIdSignStatus> status = this.billectaClient.bankIdSignCollect(EncodeTokenHeader(), referenceToken);
        if(status.getStatusCode().is2xxSuccessful()) {
            return status.getBody();
        }
        throw new RuntimeException("Could retrieve sign status: " + status.getStatusCode().toString());
    }
}
