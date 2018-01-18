package com.hedvig.external.bankID;

import bankid.RpFaultType;
import com.hedvig.external.bankID.exceptions.BankIDError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.FaultMessageResolver;
import org.springframework.ws.soap.SoapMessage;

import javax.xml.bind.JAXBElement;
import java.io.IOException;

public class RPFaultResolver implements FaultMessageResolver {

    private final Jaxb2Marshaller marshaller;
    private Logger log = LoggerFactory.getLogger(RPFaultResolver.class);

    RPFaultResolver(Jaxb2Marshaller marshaller) {

        this.marshaller = marshaller;
    }



    @Override
    public void resolveFault(WebServiceMessage message) throws IOException {

        SoapMessage soapMessage = (SoapMessage) message;


        JAXBElement<RpFaultType> errorMessages = (JAXBElement<RpFaultType>) marshaller.unmarshal(soapMessage.getSoapBody().getFault().getFaultDetail().getDetailEntries().next().getSource());

        if (errorMessages != null) {
            throw new BankIDError(errorMessages.getValue());
        }


    }
}
