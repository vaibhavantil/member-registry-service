package com.hedvig.external.bankID.bankidTypes;

import bankid.UserInfoType;
import lombok.Value;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;

@Value
public class UserInfo {

    protected String givenName;
    protected String surname;
    protected String name;
    protected String personalNumber;
    protected LocalDateTime notBefore;
    protected LocalDateTime notAfter;
    protected String ipAddress;

    public UserInfo(UserInfoType userInfo) {
        this.givenName = userInfo.getGivenName();
        this.surname = userInfo.getSurname();
        this.name = userInfo.getName();
        this.personalNumber = userInfo.getPersonalNumber();
        this.notBefore = userInfo.getNotBefore().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
        this.notAfter = userInfo.getNotAfter().toGregorianCalendar().toZonedDateTime().toLocalDateTime();
        this.ipAddress = userInfo.getIpAddress();
    }

    public UserInfo(String name, String givenName, String surname, String ssn, String ipAddress, XMLGregorianCalendar notBefore, XMLGregorianCalendar notAfter) {
        this.givenName = givenName;
        this.surname = surname;
        this.name = name;
        this.personalNumber = ssn;
        this.notBefore = toLocalDateTime(notBefore);
        this.notAfter = toLocalDateTime(notAfter);
        this.ipAddress = ipAddress;
    }

    private LocalDateTime toLocalDateTime(XMLGregorianCalendar gregorian) {
        return gregorian.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }
}
