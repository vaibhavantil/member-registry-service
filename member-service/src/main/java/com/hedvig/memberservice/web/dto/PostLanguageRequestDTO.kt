package com.hedvig.memberservice.web.dto

import java.util.Locale

data class PostLanguageRequestDTO (
     val graphqlLocale: GraphQLLocale?,
     val acceptLanguage : String?
)

enum class GraphQLLocale {
    sv_SE {
        override fun toLocale() = Locale("sv", "SE")
    },
    en_SE {
        override fun toLocale() = Locale("en", "SE")
    },
    nb_NO {
        override fun toLocale() = Locale("nb", "NO")
    },
    en_NO {
        override fun toLocale() = Locale("en", "NO")
    };

    abstract fun toLocale() : Locale
}
