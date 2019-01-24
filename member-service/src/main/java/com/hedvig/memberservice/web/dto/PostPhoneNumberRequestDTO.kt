package com.hedvig.memberservice.web.dto

import javax.validation.constraints.NotNull

data class PostPhoneNumberRequestDTO (
     @get:NotNull
     val phoneNumber:String
)
