package com.hedvig.memberservice.util

import java.util.Optional

fun <T> Optional<T>.orNull(): T? = orElse(null)
