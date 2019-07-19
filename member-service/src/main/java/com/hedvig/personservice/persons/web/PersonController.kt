package com.hedvig.personservice.persons.web

import com.hedvig.personservice.persons.PersonService
import com.hedvig.personservice.persons.model.Flag
import com.hedvig.personservice.persons.model.PersonFlags
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
    @GetMapping("/flags/{ssn}")
    fun flags(@PathVariable ssn: String): ResponseEntity<PersonFlags> {
        return ResponseEntity.ok(personService.getFlags(ssn))
    }
}
