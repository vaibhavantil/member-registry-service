package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.hedvig.memberservice.externalApi.productsPricing.ProductApi;
import lombok.val;
import org.axonframework.eventhandling.GenericEventMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class NameUpdateSagaTest {

  @Mock
  ProductApi productApi;

  @Test
  public void onNameUpdateEvent() {
    val saga = new NameUpdateSaga();
    saga.productApi = productApi;

    final NameUpdatedEvent e = new NameUpdatedEvent(1337L, "First", "Last");
    saga.onMemberNameUpdate(e);

    then(productApi).should().memberNameUpdate(1337L, "First");
  }
}
