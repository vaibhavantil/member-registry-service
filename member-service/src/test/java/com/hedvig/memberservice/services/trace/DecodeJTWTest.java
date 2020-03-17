package com.hedvig.memberservice.services.trace;

import com.hedvig.memberservice.events.EmailUpdatedEvent;
import com.hedvig.memberservice.web.dto.TraceMemberDTO;
import org.junit.Test;
import org.mockito.internal.util.collections.Iterables;

import java.time.Instant;
import java.util.Map;

import static com.hedvig.memberservice.services.trace.TraceMemberHelper.getTraceInfo;
import static org.assertj.core.api.Assertions.assertThat;

public class DecodeJTWTest {

  @Test
  public void getTraceInfo2() {

    EmailUpdatedEvent event = new EmailUpdatedEvent(1337l, "new@hedvig.com");
    Map<String, TraceMemberDTO> traceInfo = getTraceInfo(event, "1337", Instant.now(), "eyJ0eXAiOiJKV1QiLCJhbGciOiJub25lIn0.eyJlbWFpbCI6InRvbWFzQGhlZHZpZy5jb20ifQ.");
    TraceMemberDTO traceMemberDTO = Iterables.firstOf(traceInfo.values());

    assertThat(traceMemberDTO.getUserId()).isEqualTo("tomas@hedvig.com");
  }

  @Test
  public void nullTokenSetsUserIdToSystem() {

    EmailUpdatedEvent event = new EmailUpdatedEvent(1337l, "new@hedvig.com");
    Map<String, TraceMemberDTO> traceInfo = getTraceInfo(event, "1337", Instant.now(), null);
    TraceMemberDTO traceMemberDTO = Iterables.firstOf(traceInfo.values());

    assertThat(traceMemberDTO.getUserId()).isEqualTo("System");
  }

}
