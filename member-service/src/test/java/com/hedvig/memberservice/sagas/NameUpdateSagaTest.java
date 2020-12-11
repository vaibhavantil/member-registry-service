package com.hedvig.memberservice.sagas;

import com.hedvig.memberservice.events.NameUpdatedEvent;
import com.hedvig.integration.productsPricing.CampaignService;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.BDDMockito.then;

@RunWith(MockitoJUnitRunner.class)
public class NameUpdateSagaTest {

  @Mock
  CampaignService campaignService;

  @Test
  public void onNameUpdateEvent() {
    val saga = new NameUpdateSaga(campaignService);

    final NameUpdatedEvent e = new NameUpdatedEvent(1337L, "First", "Last");
    saga.onMemberNameUpdate(e);

    then(campaignService).should().memberNameUpdate(1337L, "First");
  }
}
