package com.hedvig.external.bankID;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Value("${hedvig.external.bankid.url}")
    private String bankIDUrl;
    @Value("${http.client.ssl.trust-store}")
    private File trustStore;
    @Value("${http.client.ssl.trust-store-password}")
    private String trustStorePassword;
    @Value("${http.client.ssl.key-store}")
    private File keyStore;
    @Value("${http.client.ssl.key-store-password}")
    private String keyStorePassword;

    @Bean
    Jaxb2Marshaller jaxb2Marshaller(){
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("bankid");
        return jaxb2Marshaller;
    }

    @Bean
    HttpComponentsMessageSender messageSender() {
        return new HttpComponentsMessageSender();
    }

    @Bean
    FaultMessageResolver faultMessageResolver(Jaxb2Marshaller marshaller) {
        return new RPFaultResolver(marshaller);
    }

    @Bean
    WebServiceTemplate webServiceTemplate(HttpComponentsMessageSender httpComponentsMessageSender, FaultMessageResolver faultMessageResolver) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(
                        trustStore,
                        trustStorePassword.toCharArray()
                ).loadKeyMaterial(
                        keyStore,
                        keyStorePassword.toCharArray(),
                        keyStorePassword.toCharArray()
                ).build();
        SSLConnectionSocketFactory socketFactory =
                new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
                .build();

        httpComponentsMessageSender.setHttpClient(httpClient);

        WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
        webServiceTemplate.setMessageSender(httpComponentsMessageSender);
        webServiceTemplate.setMarshaller(jaxb2Marshaller());
        webServiceTemplate.setUnmarshaller(jaxb2Marshaller());
        webServiceTemplate.setDefaultUri(bankIDUrl);
        webServiceTemplate.setFaultMessageResolver(faultMessageResolver);

        return webServiceTemplate;
    }

}
