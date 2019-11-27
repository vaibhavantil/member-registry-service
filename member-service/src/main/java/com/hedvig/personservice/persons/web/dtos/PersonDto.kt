package com.hedvig.personservice.persons.web.dtos

import com.hedvig.personservice.debts.web.dtos.DebtDto
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Person
import com.hedvig.personservice.persons.model.PersonFlags
import com.hedvig.personservice.persons.model.Whitelisted

data class PersonDto(
    val flags: PersonFlags,
    val debt: DebtDto,
    val whitelisted: Whitelisted?,
    val status: PersonStatusDto
) {
    companion object {
        fun from(person: Person, personFlags: PersonFlags): PersonDto = PersonDto(
            flags = personFlags,
            debt = DebtDto.from(person.debtSnapshots.last()),
            whitelisted = person.whitelisted,
            status = PersonStatusDto.from(
                person = person,
                personFlag = PersonService.calculateOverallFlag(personFlags)
            )
        )
    }
}
