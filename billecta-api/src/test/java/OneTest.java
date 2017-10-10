package com.hedvig;

import com.hedvig.external.billectaAPI.BillectaApi;
import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import com.hedvig.external.billectaAPI.api.BankIdStatusType;
import com.hedvig.external.billectaAPI.api.ObjectFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.Base64;
import java.util.Optional;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
/*
@RunWith(SpringRunner.class)
public class OneTest {

    private String apiKey = "apiKey";
    private String creditorId = "123";

    private String marshallToXML(BankIdAuthenticationStatus bankIdAuthenticationStatus) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BankIdAuthenticationStatus.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        StringWriter strWr = new StringWriter();
        ObjectFactory factory = new ObjectFactory();
        marshaller.marshal(factory.createBankIdAuthenticationStatus(bankIdAuthenticationStatus), strWr);
        return strWr.toString();
    }

    @Test
    public void PassBase64EncodedApiKey() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer serverMock = MockRestServiceServer.bindTo(restTemplate).build();
        BillectaApi api = new BillectaApi(creditorId, apiKey, restTemplate);

        String encodedToken = Base64.getEncoder().encodeToString(apiKey.getBytes());
        serverMock.expect(ExpectedCount.manyTimes(), requestTo("https://apitest.billecta.com/v1/bankid/authentication/ada")).
                andExpect(header("Authorization", "SecureToken " + encodedToken))
                .andRespond(withSuccess());

        api.BankIdCollect("ada");
    }

    @Test
    public void VerifyCollect() throws JAXBException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer serverMock = MockRestServiceServer.bindTo(restTemplate).build();
        BillectaApi api = new BillectaApi(creditorId, apiKey, restTemplate);

        BankIdAuthenticationStatus status = new BankIdAuthenticationStatus();
        status.setStatus(BankIdStatusType.COMPLETE);
        status.setSSN("19121212-1212");
        status.setReferenceToken("referenceToken");
        status.setAutoStartToken("autostartToken");

        serverMock.expect(ExpectedCount.once(), requestTo("https://apitest.billecta.com/v1/bankid/authentication/token")).andExpect(method(HttpMethod.GET)).andRespond(withSuccess(marshallToXML(status), MediaType.APPLICATION_XML));

        api.BankIdCollect("token");
    }

    @Test
    public void StartAuthWithoutSSN() throws JAXBException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer serverMock = MockRestServiceServer.bindTo(restTemplate).build();
        BillectaApi api = new BillectaApi(creditorId, apiKey, restTemplate);

        BankIdAuthenticationStatus status = new BankIdAuthenticationStatus();
        status.setStatus(BankIdStatusType.STARTED);
        status.setReferenceToken("referenceToken");
        status.setAutoStartToken("autostartToken");

        serverMock.expect(ExpectedCount.once(), requestTo(String.format("https://apitest.billecta.com/v1/bankid/authentication/%s?ssn=",creditorId)))
                .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess(marshallToXML(status), MediaType.APPLICATION_XML));

        api.BankIdAuth(Optional.empty());
    }

    @Test
    public void StartAuthWithSSN() throws JAXBException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer serverMock = MockRestServiceServer.bindTo(restTemplate).build();
        BillectaApi api = new BillectaApi(creditorId, apiKey, restTemplate);

        BankIdAuthenticationStatus status = new BankIdAuthenticationStatus();
        status.setStatus(BankIdStatusType.STARTED);
        status.setReferenceToken("referenceToken");
        status.setAutoStartToken("autostartToken");

        String tolvanSSN = "191212121212";

        serverMock.expect(ExpectedCount.once(), requestTo(String.format("https://apitest.billecta.com/v1/bankid/authentication/%s?ssn=%s",creditorId, tolvanSSN)))
                .andExpect(method(HttpMethod.PUT)).andRespond(withSuccess(marshallToXML(status), MediaType.APPLICATION_XML));

        api.BankIdAuth(Optional.of(tolvanSSN));
    }


    @Test
    public void CollectReturns400() throws JAXBException {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer serverMock = MockRestServiceServer.bindTo(restTemplate).build();
        BillectaApi api = new BillectaApi("creditorId", "apiKey", restTemplate);

        BankIdAuthenticationStatus status = new BankIdAuthenticationStatus();
        status.setStatus(BankIdStatusType.COMPLETE);
        status.setSSN("19121212-1212");
        status.setReferenceToken("referenceToken");
        status.setAutoStartToken("autostartToken");

        serverMock.expect(ExpectedCount.once(), requestTo("https://apitest.billecta.com/v1/bankid/authentication/token")).andExpect(method(HttpMethod.GET)).andRespond(withStatus(HttpStatus.BAD_REQUEST));

        api.BankIdCollect("token");
    }
}*/

