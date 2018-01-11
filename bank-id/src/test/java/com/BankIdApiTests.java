package com;

import bankid.*;
import com.hedvig.external.bankID.BankIdApi;
import com.hedvig.external.bankID.BankIdClient;
import com.hedvig.external.bankID.bankidTypes.CollectResponse;
import com.hedvig.external.bankID.bankidTypes.OrderResponse;
import com.hedvig.external.bankID.bankidTypes.ProgressStatus;
import com.hedvig.external.bankID.exceptions.BankIDError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class BankIdApiTests {

    @MockBean
    BankIdClient bankIdClient;


    @Test
    public void TestAuth() {

        OrderResponseType orderResponseType = new OrderResponseType();
        final String orderReference = "orderReference";
        orderResponseType.setOrderRef(orderReference);
        final String autostartToken = "autostartToken";
        orderResponseType.setAutoStartToken(autostartToken);

        when(bankIdClient.auth(null)).thenReturn(orderResponseType);

        BankIdApi api = new BankIdApi(bankIdClient);
        OrderResponse response = api.auth();

        assertThat(response).isNotNull();
        assertThat(response.getAutoStartToken()).isEqualTo(autostartToken);
        assertThat(response.getOrderRef()).isEqualTo(orderReference);
    }

    @Test
    public void TestSign() throws UnsupportedEncodingException {

        OrderResponseType orderResponseType = new OrderResponseType();
        final String orderReference = "orderReference";
        orderResponseType.setOrderRef(orderReference);
        final String autostartToken = "autostartToken";
        orderResponseType.setAutoStartToken(autostartToken);

        final String ssn = "1212121212";

        final String message = "A short but nice message!";

        when(bankIdClient.sign(ssn, message)).thenReturn(orderResponseType);

        BankIdApi api = new BankIdApi(bankIdClient);
        OrderResponse response = api.sign(ssn, message);

        assertThat(response).isNotNull();
        assertThat(response.getAutoStartToken()).isEqualTo(autostartToken);
        assertThat(response.getOrderRef()).isEqualTo(orderReference);

    }

    @Test(expected = BankIDError.class)
    public void TestSignWithError() throws UnsupportedEncodingException {


        RpFaultType rpFaultType = new RpFaultType();
        rpFaultType.setFaultStatus(FaultStatusType.INVALID_PARAMETERS);
        rpFaultType.setDetailedDescription("Invalid SSN");

        final String ssn = "1212121212";
        final String message = "A short but nice message!";

        when(bankIdClient.sign(ssn, message)).thenThrow(new BankIDError(rpFaultType));

        BankIdApi api = new BankIdApi(bankIdClient);
        api.sign(ssn, message);
    }

    @Test
    public void Collect() throws DatatypeConfigurationException {
        final String orderReference = "orderReference";

        CollectResponseType crt = new CollectResponseType();
        crt.setOcspResponse("oscpResponse");
        crt.setProgressStatus(ProgressStatusType.COMPLETE);
        crt.setSignature("signature");

        UserInfoType uit = new UserInfoType();
        uit.setGivenName("FirstName");
        uit.setSurname("LastName");
        uit.setName("FirstName LastName");
        ZonedDateTime today = ZonedDateTime.now();

        uit.setNotBefore(createXMLGregorian(today));
        uit.setNotAfter(createXMLGregorian(today.plusDays(10)));
        crt.setUserInfo(uit);

        when(bankIdClient.collect(orderReference)).thenReturn(crt);

        BankIdApi api = new BankIdApi(bankIdClient);
        CollectResponse response = api.collect(orderReference);

        assertThat(response).isNotNull();
        assertThat(response.getProgressStatus()).isEqualTo(ProgressStatus.COMPLETE);

    }

    @Test
    public void Collect_BankIdSession_Pending() throws DatatypeConfigurationException {
        final String orderReference = "orderReference";

        CollectResponseType crt = new CollectResponseType();
        crt.setProgressStatus(ProgressStatusType.OUTSTANDING_TRANSACTION);


        when(bankIdClient.collect(orderReference)).thenReturn(crt);

        BankIdApi api = new BankIdApi(bankIdClient);
        CollectResponse response = api.collect(orderReference);

        assertThat(response).isNotNull();
        assertThat(response.getProgressStatus()).isEqualTo(ProgressStatus.OUTSTANDING_TRANSACTION);

    }

    private XMLGregorianCalendar createXMLGregorian(ZonedDateTime dateTime) throws DatatypeConfigurationException {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(dateTime));
    }

}
