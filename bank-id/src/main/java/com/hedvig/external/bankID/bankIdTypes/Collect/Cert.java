package com.hedvig.external.bankID.bankIdTypes.Collect;

import lombok.Value;

@Value
public class Cert {
  private long notBefore;
  private long notAfter;
}
