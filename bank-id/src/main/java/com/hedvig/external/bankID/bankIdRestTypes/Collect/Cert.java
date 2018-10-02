package com.hedvig.external.bankID.bankIdRestTypes.Collect;

import lombok.Value;

@Value
public class Cert {
  private long notBefore;
  private long notAfter;
}
