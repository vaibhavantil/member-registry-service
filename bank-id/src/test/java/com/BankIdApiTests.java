package com;

import com.hedvig.external.bankID.bankId.BankIdApi;
import com.hedvig.external.bankID.bankId.BankIdApiImpl;
import com.hedvig.external.bankID.bankId.BankIdClient;
import com.hedvig.external.bankID.bankIdTypes.BankIdError;
import com.hedvig.external.bankID.bankIdTypes.BankIdErrorType;
import com.hedvig.external.bankID.bankIdTypes.Collect.User;
import com.hedvig.external.bankID.bankIdTypes.CollectRequest;
import com.hedvig.external.bankID.bankIdTypes.CollectResponse;
import com.hedvig.external.bankID.bankIdTypes.CollectStatus;
import com.hedvig.external.bankID.bankIdTypes.CompletionData;
import com.hedvig.external.bankID.bankIdTypes.OrderAuthRequest;
import com.hedvig.external.bankID.bankIdTypes.OrderResponse;
import com.hedvig.external.bankID.bankIdTypes.OrderSignRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
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

    final String orderReference = "orderReference";
    final String autostartToken = "autostartToken";
    OrderResponse orderResponse = new OrderResponse(orderReference, autostartToken);

    OrderAuthRequest orderAuthRequest = new OrderAuthRequest("0.0.0.0");

    when(bankIdClient.auth(orderAuthRequest)).thenReturn(ResponseEntity.ok(orderResponse));

    BankIdApi api = new BankIdApiImpl(bankIdClient);
    OrderResponse response = api.auth(orderAuthRequest);

    assertThat(response).isNotNull();
    assertThat(response.getAutoStartToken()).isEqualTo(autostartToken);
    assertThat(response.getOrderRef()).isEqualTo(orderReference);
  }

  @Test
  public void TestSign() {

    final String orderReference = "orderReference";
    final String autostartToken = "autostartToken";
    OrderResponse orderResponse = new OrderResponse(orderReference, autostartToken);

    final String ssn = "1212121212";

    final String message = "A short but nice message!";

    final String endUserIp = "0.0.0.0";

    when(bankIdClient.sign(new OrderSignRequest(ssn, endUserIp, message))).thenReturn(ResponseEntity.ok(orderResponse));

    BankIdApi api = new BankIdApiImpl(bankIdClient);
    OrderResponse response = api.sign(ssn, endUserIp, message);

    assertThat(response).isNotNull();
    assertThat(response.getAutoStartToken()).isEqualTo(autostartToken);
    assertThat(response.getOrderRef()).isEqualTo(orderReference);
  }

  @Test(expected = BankIdError.class)
  public void TestSignWithError() throws UnsupportedEncodingException {
    
    final String ssn = "1212121212";
    final String message = "A short but nice message!";
    final String endUserIp = "0.0.0.0";

    when(bankIdClient.sign(new OrderSignRequest(ssn, endUserIp, message))).thenThrow(new BankIdError(BankIdErrorType.UNKNOWN));

    BankIdApi api = new BankIdApiImpl(bankIdClient);
    api.sign(ssn, endUserIp, message);
  }

  @Test
  public void Collect() throws DatatypeConfigurationException {
    final String orderReference = "orderReference";
    final String ssn = "1212121212";
    final String firstName = "FirstName";
    final String lastName = "LastName";
    final String name = "FirstName LastName";

    User user = new User(ssn, firstName, lastName, name);

    CompletionData completionData = new CompletionData(user, null, null, "signature", "oscpResponse");

    CollectResponse collectResponse = new CollectResponse(orderReference, CollectStatus.complete, null, completionData);
    CollectRequest collectRequest = new CollectRequest(orderReference);

    when(bankIdClient.collect(collectRequest)).thenReturn(ResponseEntity.ok(collectResponse));

    BankIdApi api = new BankIdApiImpl(bankIdClient);
    CollectResponse response = api.collect(collectRequest);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(CollectStatus.complete);
  }

  @Test
  public void Collect_BankIdSession_Pending() throws DatatypeConfigurationException {
    final String orderReference = "orderReference";

    CollectResponse collectResponse = new CollectResponse(orderReference, CollectStatus.pending, "outstandingTransaction", null);
    CollectRequest collectRequest = new CollectRequest(orderReference);

    when(bankIdClient.collect(collectRequest)).thenReturn(ResponseEntity.ok(collectResponse));

    BankIdApi api = new BankIdApiImpl(bankIdClient);
    CollectResponse response = api.collect(collectRequest);

    assertThat(response).isNotNull();
    assertThat(response.getStatus()).isEqualTo(CollectStatus.pending);
    assertThat(response.getHintCode()).isEqualTo("outstandingTransaction");
  }

  private XMLGregorianCalendar createXMLGregorian(ZonedDateTime dateTime)
    throws DatatypeConfigurationException {
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(GregorianCalendar.from(dateTime));
  }
}
