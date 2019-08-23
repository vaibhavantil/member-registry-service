package com.hedvig.personservice.persons.model

import com.hedvig.personservice.debts.model.DebtSnapshot
import java.util.*
import javax.persistence.*

@Entity
class Person(
    @Id
    val id: UUID,
    @Column(unique = true)
    val ssn: String,
    @OneToMany(mappedBy = "person")
    val debtSnapshots: MutableList<DebtSnapshot>,
    @Embedded
    var whitelisted: Whitelisted? = null
)
