package com.hedvig.memberservice.web.dto

import javax.validation.constraints.Email
import javax.validation.constraints.NotNull

data class PostEmailRequestDTO(
    @get:NotNull @get:Email
    val email:String
)
