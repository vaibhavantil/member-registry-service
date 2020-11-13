package com.hedvig.memberservice.aggregates

import java.util.Locale

@Suppress("EnumEntryName")
enum class PickedLocale {
    sv_SE {
        override val locale: Locale = Locale("sv", "SE")
    },
    en_SE {
        override val locale: Locale = Locale("en", "SE")
    },
    nb_NO{
        override val locale: Locale = Locale("nb", "NO")
    },
    en_NO {
        override val locale: Locale = Locale("en", "NO")
    },
    da_DK {
        override val locale: Locale = Locale("da", "DK")
    },
    en_DK {
        override val locale: Locale = Locale("en", "DK")
    };

    abstract val locale: Locale
}

fun Locale.toPickedLocale() = when {
    this.country == "SE" && this.language == Locale("sv").language -> PickedLocale.sv_SE
    this.country == "SE" && this.language == Locale("en").language -> PickedLocale.en_SE
    this.country == "NO" && this.language == Locale("nb").language -> PickedLocale.nb_NO
    this.country == "NO" && this.language == Locale("en").language -> PickedLocale.en_NO
    this.country == "DK" && this.language == Locale("da").language -> PickedLocale.da_DK
    this.country == "DK" && this.language == Locale("en").language -> PickedLocale.en_DK
    else -> throw RuntimeException("No matching picked_locale for [Locale: $this]")
}
