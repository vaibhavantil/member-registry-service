package com.hedvig.external.bisnodeBCI;

import com.hedvig.external.bisnodeBCI.dto.Person;
import com.hedvig.external.bisnodeBCI.dto.PersonSearchResultListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;
import sun.misc.BASE64Encoder;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BisnodeClient {

    private final String clientId;
    private final String clientSecret;
    private final RestTemplate template;
    private final String baseUrl = "https://api.bisnode.com/consumerintelligence/person/v2";
    private Instant expiresAs = Instant.now();
    private String accessToken = "";
    private Logger logger =  LoggerFactory.getLogger(BisnodeClient.class);

    public BisnodeClient(String clientId, String clientSecret, RestTemplate template) {

        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.template = template;
    }

    public PersonSearchResultListResponse match(String ssn) throws RestClientException {

        HttpHeaders headers = getHeaders();
        HttpEntity<String> entity = new HttpEntity<>("",headers);


        UriTemplate uriTemplate = new UriTemplate(baseUrl + "/?sourceCountry=SE&legalId={legalId}");
        ResponseEntity<PersonSearchResultListResponse> response = template.exchange(
                uriTemplate.expand(new HashMap<String,String>(){{put("legalId", ssn);}}),
                HttpMethod.GET,
                entity,
                PersonSearchResultListResponse.class);

        logger.info(response.toString());

        return response.getBody();
    }

    private HttpHeaders getHeaders() {

        if(expiresAs.isBefore(Instant.now())) {
            Authorize();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void Authorize() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String authString = clientId + ":" + clientSecret;
        String encodedAuth = new String(Base64Utils.encode(authString.getBytes()));
        headers.add("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> variables = new LinkedMultiValueMap<String, String>();

        variables.add("grant_type", "client_credentials");
        variables.add("scope", "bci");


        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(variables, headers);

        ResponseEntity<AccessToken> token = template.exchange("https://login.bisnode.com/as/token.oauth2", HttpMethod.POST, request, AccessToken.class);

        if (token.getStatusCode() == HttpStatus.OK) {
            Instant now = Instant.now();
            now = now.plusSeconds(token.getBody().getExpires_in());
            now = now.minusSeconds(60);

            expiresAs = now;
            accessToken = token.getBody().getAccess_token();
        }
    }
}
