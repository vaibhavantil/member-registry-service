package com.hedvig.external.bisnodeBCI.dto;


import lombok.Value;

import java.time.LocalDate;
import java.util.List;

@Value
public class Person {

    private String gedi;
    private String legalId;
    private Boolean privateIdentity;
    private List<String> firstNames;
    private String preferredFirstName;
    private String familyName;
    private String additionalName;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private Boolean deceased;
    private String dateOfDeath;
    private Boolean directMarketingRestriction;

    private List<Address> addressList;

    private List<Telephone> phoneList;

    public String getPreferredOrFirstName() throws RuntimeException {
      if (getPreferredFirstName() == null) {
        if (getFirstNames() == null || getFirstNames().size() == 0) {
          throw new RuntimeException(
              "Could not find firstname in bisnode response, prefferedFirstName and firstNames are null");
        }
        return getFirstNames().get(0);
      }

      return getPreferredFirstName();
    }
}
