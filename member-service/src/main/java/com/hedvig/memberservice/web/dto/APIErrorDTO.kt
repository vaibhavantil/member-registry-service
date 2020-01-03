package com.hedvig.memberservice.web.dto

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import org.springframework.http.HttpStatus

@JsonTypeName("apiError")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
data class APIErrorDTO(
    val status: HttpStatus,
    val code: String,
    val message: String
)
