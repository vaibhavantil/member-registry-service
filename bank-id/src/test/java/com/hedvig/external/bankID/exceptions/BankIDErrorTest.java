package com.hedvig.external.bankID.exceptions;

import bankid.FaultStatusType;
import bankid.RpFaultType;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


@SuppressWarnings("ThrowableNotThrown")
public class BankIDErrorTest {

    @Test
    public void Something(){

        final String faultDetail = "Some good reason";
        final FaultStatusType faultType = FaultStatusType.EXPIRED_TRANSACTION;

        RpFaultType generatedFault = new RpFaultType();
        generatedFault.setDetailedDescription(faultDetail);
        generatedFault.setFaultStatus(faultType);


        BankIDError errorToTest = new BankIDError(generatedFault);
        assertThat(errorToTest.detail).isEqualTo(faultDetail);
        assertThat(errorToTest.errorType).isEqualTo(BankIDError.ErrorType.EXPIRED_TRANSACTION);

    }

}