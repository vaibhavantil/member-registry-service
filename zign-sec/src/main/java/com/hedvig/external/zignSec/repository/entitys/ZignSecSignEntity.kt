package com.hedvig.external.zignSec.repository.entitys

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints=[UniqueConstraint(columnNames=["personalNumber", "idProviderPersonId"])])
class ZignSecSignEntity(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val internalId: Long = 0,
    @Column(columnDefinition = "varchar(11)")
    val personalNumber: String,
    val idProviderPersonId: String
) {
    constructor() : this(
        personalNumber = "",
        idProviderPersonId = ""
    )
}

