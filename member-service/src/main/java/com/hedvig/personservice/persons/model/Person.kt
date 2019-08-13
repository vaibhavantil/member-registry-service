package com.hedvig.personservice.persons.model

import com.hedvig.personservice.debts.model.DebtSnapshot
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
class Person(
    @Id
    val ssn: String,
    @OneToMany(mappedBy = "person")
    val debtSnapshots: MutableList<DebtSnapshot>,
    var whitelisted: Whitelisted? = null
)
