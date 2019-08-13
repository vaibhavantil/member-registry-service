package com.hedvig.personservice.persons.web

import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.web.dtos.PersonDto
import com.hedvig.personservice.persons.web.dtos.PersonStatusDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

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

    @PostMapping("/member/whitelist/{memberId}")
    fun whitelistMember(
        @PathVariable memberId: String,
        @RequestParam whitelistedBy: String
    ): ResponseEntity<Void> {
        personService.whitelistPersonByMemberId(memberId, whitelistedBy)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/member/status/{memberId}")
    fun getPersonStatus(@PathVariable memberId: String): ResponseEntity<PersonStatusDto> {
        val person = personService.getPersonOrNullByMemberId(memberId)
            ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(PersonStatusDto.from(person))
    }
}
