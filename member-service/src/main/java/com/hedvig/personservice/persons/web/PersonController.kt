package com.hedvig.personservice.persons.web

import com.hedvig.memberservice.query.MemberRepository
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.PersonFlags
import com.hedvig.personservice.persons.web.dtos.PersonDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/person")
class PersonController @Autowired constructor(
        private val personService: PersonService
) {
    @GetMapping("/member/{memberId}")
    fun getPersonByMemberId(@PathVariable memberId: String): ResponseEntity<PersonDto> {
        val person = personService.getPersonOrNullByMemberId(memberId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(PersonDto.from(person))
    }

    @GetMapping("/{ssn}")
    fun getPerson(@PathVariable ssn: String): ResponseEntity<PersonDto> {
        val person = personService.getPersonOrNull(ssn)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(PersonDto.from(person))
    }
}
