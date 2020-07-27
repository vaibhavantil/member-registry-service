package com.hedvig.memberservice.services.exceptions

import java.lang.RuntimeException

class CountryNotFoundException(country: String) : RuntimeException(
    "No country found by name $country"
)
