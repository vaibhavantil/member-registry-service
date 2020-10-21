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
