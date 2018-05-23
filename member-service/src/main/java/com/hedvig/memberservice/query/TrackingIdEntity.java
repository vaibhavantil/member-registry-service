package com.hedvig.memberservice.query;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class TrackingIdEntity {
    @Id
    @Getter
    @Setter
    @Column(unique = true)
    private Long memberId;
    
    @Getter
    @Setter
    @Column(unique = true)
    private UUID trackingId;
}
