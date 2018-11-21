package com.hedvig.memberservice.services.trace;

import com.hedvig.memberservice.events.Traceable;
import com.hedvig.memberservice.web.dto.TraceMemberDTO;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class TraceMemberServiceImpl implements TraceMemberService {

  private EventStorageEngine eventStorageEngine;

  @Autowired
  public TraceMemberServiceImpl(EventStorageEngine eventStorageEngine) {
    this.eventStorageEngine = eventStorageEngine;
  }

  @Override
  public List<TraceMemberDTO> getTracesByMemberId(Long memberId) {
    final List<TraceMemberDTO> result = new ArrayList();
    final Map<String, TraceMemberDTO> uniqResults = new HashMap<>();
    eventStorageEngine.readEvents(memberId.toString())
      .asStream().filter((EventMessage m) -> m.getPayload() instanceof Traceable && ((Traceable)m.getPayload()).getMemberId().equals(memberId))
      .iterator().forEachRemaining((EventMessage m) ->
      uniqResults.putAll(
            TraceMemberHelper.getTraceInfo((Traceable)m.getPayload(),
              memberId.toString(),
              m.getTimestamp(),
              m.getMetaData().get("token")
            )
          )
    );
    result.addAll(uniqResults.values());
    result.sort(Comparator.comparing(TraceMemberDTO::getDate));

    return result;
  }

}
