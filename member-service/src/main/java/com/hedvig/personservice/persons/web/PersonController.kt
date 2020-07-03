package com.hedvig.personservice.persons.web

import com.hedvig.personservice.persons.web.dtos.HasPersonSignedBeforeRequest
import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.PersonFlags
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
    @GetMapping("/{ssn}")
    fun getPerson(@PathVariable ssn: String): ResponseEntity<PersonDto> {
        val person = personService.getPersonOrNull(ssn)
            ?: return ResponseEntity.notFound().build()
        val personFlags = personService.getAllPersonFlags(person)
        return ResponseEntity.ok(PersonDto.from(person, personFlags))
    }

    @GetMapping("/member/{memberId}")
    fun getPersonByMemberId(@PathVariable memberId: String): ResponseEntity<PersonDto> {
        val person = personService.getPersonOrNullByMemberId(memberId)
            ?: return ResponseEntity.notFound().build()
        val personFlags = personService.getAllPersonFlags(person)
        return ResponseEntity.ok(PersonDto.from(person, personFlags))
    }

    @PostMapping("/whitelist/{ssn}")
    fun whitelistPerson(
        @PathVariable ssn: String,
        @RequestParam whitelistedBy: String
    ): ResponseEntity<Void> {
        personService.whitelistPerson(ssn, whitelistedBy)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/member/whitelist/{memberId}")
    fun whitelistPersonByMemberId(
        @PathVariable memberId: String,
        @RequestParam whitelistedBy: String
    ): ResponseEntity<Void> {
        personService.whitelistPersonByMemberId(memberId, whitelistedBy)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/member/whitelist/{memberId}/remove")
    fun removeWhitelistByMemberId(
        @PathVariable memberId: String,
        @RequestParam removedBy: String
    ): ResponseEntity<Void> {
        personService.removeWhitelistByMemberId(memberId, removedBy)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/status/{ssn}")
    fun getPersonStatus(@PathVariable ssn: String): ResponseEntity<PersonStatusDto> {
        val person = personService.getPersonOrNull(ssn)
            ?: return ResponseEntity.notFound().build()
        val personFlag = personService.getPersonFlag(person)
        return ResponseEntity.ok(PersonStatusDto.from(person, personFlag))
    }

    @GetMapping("/member/status/{memberId}")
    fun getPersonStatusByMemberId(@PathVariable memberId: String): ResponseEntity<PersonStatusDto> {
        val person = personService.getPersonOrNullByMemberId(memberId)
            ?: return ResponseEntity.notFound().build()
        val personFlag = personService.getPersonFlag(person)
        return ResponseEntity.ok(PersonStatusDto.from(person, personFlag))
    }

    @GetMapping("/member/flags/{memberId}")
    fun getPersonFlagsByMemberId(@PathVariable memberId: String): ResponseEntity<PersonFlags> {
        val person = personService.getPersonOrNullByMemberId(memberId)
            ?: return ResponseEntity.notFound().build()
        val personFlags = personService.getAllPersonFlags(person)
        return ResponseEntity.ok(personFlags)
    }

    @PostMapping("/has/signed")
    fun hasSigned(@RequestBody request: HasPersonSignedBeforeRequest): ResponseEntity<Boolean> {
        val hasSigned = personService.hasSigned(request.ssn, request.email)
        return ResponseEntity.ok(hasSigned)
    }
}
