package com.hedvig.memberservice.query;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CollectType {

    public enum BankIdRequestType {AUTH,SIGN};

    @Id
    public String referenceToken;
    public BankIdRequestType type;
}
