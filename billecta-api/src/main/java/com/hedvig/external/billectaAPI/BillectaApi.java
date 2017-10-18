package com.hedvig.external.billectaAPI;

import com.hedvig.external.billectaAPI.api.BankIdAuthenticationStatus;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class BillectaApi {

    private RestTemplate restTemplate;
    private final String baseUrl = "https://api.billecta.com/v1/";
    private final String creditorId;
    private final String secureToken;

    public BillectaApi(String creditorId, String secureToken, RestTemplate restTemplate) {
        this.creditorId = creditorId;
        this.secureToken = secureToken;
        this.restTemplate = restTemplate;
    }

    public BankIdAuthenticationStatus BankIdAuth(String ssn) {
        HttpEntity<String> entity = createHeaders();

        String ssnOrEmpty = ssn == null ? "": ssn;


        UriTemplate uri = new UriTemplate(baseUrl + "bankid/authentication/{creditorId}?ssn={ssn}");
        System.out.println(uri.expand(new HashMap<String,Object>(){{put("creditorId", creditorId); put("ssn", ssnOrEmpty);}}).toString());

        ResponseEntity<BankIdAuthenticationStatus> status = restTemplate.exchange(
                uri.expand(new HashMap<String,Object>(){{put("creditorId", creditorId); put("ssn", ssnOrEmpty);}}),
                HttpMethod.PUT,
                entity,
                BankIdAuthenticationStatus.class
                );

        return status.getBody();
    }

    private HttpEntity<String> createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SecureToken " + Base64.getEncoder().encodeToString(secureToken.getBytes()));
        headers.setContentType(MediaType.APPLICATION_XML);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
        return new HttpEntity<>("", headers);
    }

    public BankIdAuthenticationStatus BankIdCollect(String token) {
        HttpEntity<String> entity = createHeaders();

        ResponseEntity<BankIdAuthenticationStatus> status = restTemplate.exchange(
                baseUrl + "bankid/authentication/{token}",
                HttpMethod.GET,
                entity,
                BankIdAuthenticationStatus.class,
                token);

        return status.getBody();
    }
}
