package com;

import bankid.OrderResponseType;
import com.hedvig.external.bankID.BankIdClient;
import com.hedvig.external.bankID.exceptions.BankIDError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.WebServiceTemplate;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

@SpringBootTest()
@ContextConfiguration(classes = {com.hedvig.external.bankID.Configuration.class})
@RunWith(SpringRunner.class)
public class IntegrationTests {

    public final static String emoji_closed_lock_with_key = new String(new byte[]{(byte)0xF0, (byte)0x9F, (byte)0x94, (byte)0x90}, Charset.forName("UTF-8"));

    @Autowired
    WebServiceTemplate webServiceTemplate;


    @Test
    public void signWithSSN() throws UnsupportedEncodingException {
        BankIdClient bic = new BankIdClient(webServiceTemplate);//new BankIdClient();

        StringBuilder s = new StringBuilder();
        for(int i =0; i < 100; i++) {


                s.append("It was the best of times, it was the worst of times, ")
                    .append("it was the age of wisdom, it was the age of foolishness, ")
                    .append("it was the epoch of belief, it was the epoch of incredulity, ")
                    .append("it was the season of Light, it was the season of Darkness, ")
                    .append("it was the spring of hope, it was the winter of despair, ")
                    .append("we had everything before us, we had nothing before us.\n");
        }


        try {
            OrderResponseType response = bic.sign("1965072723569", s.toString() + emoji_closed_lock_with_key);
            System.out.println(response.getOrderRef());
            System.out.println(response.getAutoStartToken());
        }catch (WebServiceIOException ex) {
            System.out.println(ex);
            throw ex;
        }catch (BankIDError ex) {
            System.out.println(ex.errorType + ", " + ex.detail);
        }
    }

    /*
    @Test public void collectWithOrderRef() {
        BankIdClient bankIdClient = new BankIdClient(webServiceTemplate);

        OrderResponseType response = bankIdClient.auth("196507272356");
        bankid.CollectResponseType  collectResponseType = bankIdClient.collect(response.getOrderRef());
        System.out.println(collectResponseType.getProgressStatus().value());
    }*/
}