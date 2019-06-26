package com.hedvig.personservice.persons.query

import com.hedvig.personservice.persons.model.Person
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface PersonRepository: CrudRepository<Person, String>
