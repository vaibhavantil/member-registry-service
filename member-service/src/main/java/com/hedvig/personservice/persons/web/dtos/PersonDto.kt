package com.hedvig.personservice.persons.web.dtos

import com.hedvig.personservice.debts.web.dtos.DebtDto
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.PersonFlags

data class PersonDto(
    val flags: PersonFlags,
    val debt: DebtDto
) {
    companion object {
        fun from(person: Person): PersonDto = PersonDto(
            flags = PersonService.getFlags(person),
            debt = DebtDto.from(person.debtSnapshots.last())
        )
    }
}
