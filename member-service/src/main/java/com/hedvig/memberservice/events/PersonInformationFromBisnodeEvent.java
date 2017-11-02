package com.hedvig.memberservice.events;

import com.hedvig.memberservice.aggregates.BisnodeInformation;
import lombok.Value;

@Value
public class PersonInformationFromBisnodeEvent {
    Long memberId;
    BisnodeInformation information;
    public PersonInformationFromBisnodeEvent(Long memberId, BisnodeInformation pi) {
         this.memberId = memberId;
         this.information = pi;
    }
}
