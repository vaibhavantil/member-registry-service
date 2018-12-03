package com.hedvig.memberservice.events;

import java.util.Map;

public interface Traceable {
  Long getMemberId();
  Map<String,Object> getValues ();
}
